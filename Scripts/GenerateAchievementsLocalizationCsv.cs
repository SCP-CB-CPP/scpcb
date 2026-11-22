using System.Collections.Frozen;
using System.Text.RegularExpressions;

var mode = int.Parse(args.Length > 0 ? args[0] : Query("Enter generation mode (0 = achievements): "));
var dir = args.Length > 1 ? args[1] : Query("Enter the path of strings.ini: ");

switch (mode) {
    case 0:
        GenerateAchievementsCsv();
        break;
    default:
        Console.Error.WriteLine("Unknown mode!");
        break;
}

string Query(string queryStr) {
    Console.Error.Write(queryStr);
    return Console.ReadLine()!;
}

void GenerateAchievementsCsv() {
    var files = GetFiles(dir, "2178380_loc_*.vdf", Patterns.File());

    Console.Write("api_name,field");
    foreach (var lang in files
                 .Select(x => x.Lang)) {
        Console.Write(',');
        Console.Write(lang);
    }

    var entries = files
        .AsParallel()
        .SelectMany(x => ParseFile(x.Path, x.Lang))
        .GroupByMaintainingOrder(x => (x.Key, IsDesc: x.isDesc))
        .ToArray();

    foreach (var values in entries) {
        Console.WriteLine();    
        Console.Write(values.Key.Key);
        Console.Write(',');
        Console.Write(values.Key.IsDesc ? "description" : "name");
    
        var dic = values.Entries.ToFrozenDictionary(x => x.Language, x => x.Value);
    
        foreach (var (_, lang) in files) {
            Console.Write(',');
            Console.Write(Sanitize(dic.GetValueOrDefault(lang, "")));
        }
    }
}

(string Path, string Lang)[] GetFiles(string dir, string searchPattern, Regex fileRegex)
    => Directory.EnumerateDirectories(dir)
        .AsParallel()
        .SelectMany(x => Directory.EnumerateFiles(x, searchPattern))
        .Select(x => (Path: x, Match: fileRegex.Match(Path.GetFileName(x))))
        .Where(x => x.Match.Success)
        .Select(x => (x.Path, Lang: x.Match.Groups["Lang"].Value))
        .OrderBy(x => x.Lang, StringComparer.InvariantCultureIgnoreCase)
        .ToArray();

IEnumerable<(string Language, string Key, string Value, bool isDesc)> ParseFile(string file, string lang) {
    return File.ReadLines(file)
        .Select(x => Patterns.Entry().Match(x))
        .Where(x => x.Success)
        .Select(x => (lang, x.Groups["Key"].Value, x.Groups["Value"].Value, IsDesc(x)));

    bool IsDesc(Match m) {
        var group = m.Groups["Suffix"];
        if (!group.Success) {
            throw new($"File {file} has entry {m.Value} without suffix?");
        }
        return group.ValueSpan switch {
            "DESC" => true,
            "NAME" => false,
            _ => throw new($"File {file} has entry {m.Value} with unknown suffix?"),
        };
    }
}

// WTF is this Valve??
string Sanitize(string str) {
    var ret = str.Replace("\\\"", "\"\"");
    if (ret.Contains(',') || ret.Contains('"')) {
        ret = $"\"{ret}\"";
    }
    return ret;
}

public static class Extensions {
    public static List<(TKey Key, List<TSource> Entries)> GroupByMaintainingOrder<TSource, TKey>(
        this IEnumerable<TSource> source, Func<TSource, TKey> keySelector)
        where TKey : IEquatable<TKey> {
        var groups = new List<(TKey Key, List<TSource> Entries)>();
        foreach (var item in source) {
            var key = keySelector(item);
            var idx = groups.TakeWhile(x => !x.Key.Equals(key)).Count();
            if (idx == groups.Count) {
                groups.Add((key, [item]));
            } else {
                groups[idx].Entries.Add(item);
            }
        }
        return groups;
    }
}

public static partial class Patterns {
    [GeneratedRegex(@"2178380_loc_(?<Lang>[^_]*)\.vdf")]
    public static partial Regex File();
    
    [GeneratedRegex(@"\s*""(?<Key>.*?)(_(?<Suffix>[^_]*))""\s*""(?<Value>.*[^\\])""")]
    public static partial Regex Entry();
}
