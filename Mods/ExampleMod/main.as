using namespace CB;
using namespace B3D;

Music m;
Achievement BonkAchv;
Skybox Sky;

void Hook_Initialize() {
    m = Music::RegisterCustom("SFX\\Radio\\scpradio0.ogg");
    BonkAchv = Achievement::Create("Bonk", false);
    BonkAchv.InsertBefore(Achievement::AchvConsole);
}

bool isNested = false;

// Remove checkpoints.
bool Hook_MapCreateLayout() {
    if (isNested) return false;

    // Prevent infinite recursion.
    isNested = true;
    Map::CreateLayout();
    isNested = false;

    for (int x = 0; x < Map::Width; x++) {
        for (int y = 0; y < Map::Height; y++) {
            if (Map::Temporary[x, y] == 255) {
                Map::Temporary[x, y] = 1;
            }
        }
    }

    return true;
}

int Hook_CanUseDoor(Door d, bool showMsg, bool playSFX) {
    // The key is secretly also SCP-005!
    if (Player::SelectedItem != null && Player::SelectedItem.Template.Name == "scp860") {
        Player::SelectedItem = null;
        return 1;
    }
    return -1;
}

bool Hook_InitializeEvents() {
    Event::Create("guardspin", "room3pit", 0, 1);
    Event::Create("914", "914", 0, 1);
    return true;
}

bool Hook_UpdateEvent(CB::Event e) {
    if (e.Name == "guardspin") {
        e.Room.Objects[1].Turn(0, FPSFactor * 10, 0);
    }
    return false;
}

bool Hook_FillRoom(CB::Room r) {
    if (r.Objects[1] == null) r.Objects[1] = CB::NPC::Create(NPC::Type::Guard, r.X, r.Y + 1, r.Z).Collider;
    // Clear all rooms except SCP-914's.
    return r.Template.Name != "914";
}

void Hook_LoadEntities() {
    Sky = Skybox::Create("GFX/map/sky/sky");
}

void Hook_NullGame() {
    Sky = null;
}

float configuredFOV = -1.f;

array<bool> keyDownCache;

bool IsKeyUp(uint key) {
    if (keyDownCache.Length < key + 1) keyDownCache.Resize(key + 1);

    bool actuallyDown = KeyDown(key);
    bool cachedDown = keyDownCache[key];
    if (actuallyDown != cachedDown) {
        keyDownCache[key] = actuallyDown;
        return actuallyDown == false;
    }
    return false;
}

bool wasCrouched = true;

CB::Sound bonk;

void Hook_Update() {
    if (Player::Collider == null) { return; }

    if (configuredFOV == -1.f) {
        configuredFOV = Options::FOV;
    }
    Options::FOV = configuredFOV * (1.f + Player::CurrentSpeed * 5);
    if (IsKeyUp(Options::KeyBlink)) {
        configuredFOV += 10;
    }
    if (Player::Crouch != wasCrouched) {
        if (wasCrouched) {
            Player::Collider.SetRadius(0.15, 1.0);
            Player::Collider.Move(0, 1, 0);
        } else {
            Player::Collider.SetRadius(0.15, 0.3);
        }
        wasCrouched = Player::Crouch;
    }
    if (Player::CrouchState > 0.0 && !Player::Crouch) {
        for (int i = 1; i <= Player::Collider.CountCollisions(); i++) {
            if (Player::Collider.CollisionY(i) > Player::Collider.GetY() + 0.1) {
                Player::Crouch = true;
                if (bonk == null) {
                    bonk = CB::Sound::Load("SFX\\bonk.mp3");
                }
                bonk.Play();
                BonkAchv.Award();
            }
        }
    }
    Music::ShouldPlay = m;
    Sky.Update();
}

void Hook_CombineItems(Item dragged, Item onto) {
    if (dragged.Template.Name.Substring(0, 3) != "key" || onto.Template.Name.Substring(0, 3) != "key") return;

    string lvl1 = dragged.Template.Name.Substring(3, 1);
    string lvl2 = onto.Template.Name.Substring(3, 1);

    if (lvl1.Length == 0 || lvl2.Length == 0) return;

    int res = lvl1.ParseInt() + lvl2.ParseInt();
    if (res > 6) return;

    Item new = Item::Create("key" + ToString(res), Player::Camera.GetX(true), Player::Camera.GetY(true), Player::Camera.GetZ(true));
    dragged.Remove(true);
    onto.Remove(true);
}

bool Hook_LoadRoomTemplateEntity(CB::RoomTemplate rt, int version, B3D::Stream f, string name) {
    // Screens-B-Gone!
    if (name == "screen") {
        f.ReadFloat();
        f.ReadFloat();
        f.ReadFloat();
        f.ReadString();
        return true;
    }
    return false;
}

bool Hook_ExecuteConsoleCommand(string cmd) {
    if (cmd == "ping") {
        Console::CreateMessage("Pong!");
        return true;
    }
    return false;
}

bool Hook_Use914(CB::Item item, CB::SCP914::Setting setting, float x, float y, float z) {
    if (item.Template.Name.Substring(0, 3) == "key" && setting == CB::SCP914::Setting::VeryFine) {
        Item::Create("key6", x, y, z);
        item.Remove();
        return true;
    }

    return false;
}
