using namespace CB;
using namespace B3D;

Music m;
Achievement BonkAchv;

void Hook_Initialize() {
    m = Music::RegisterCustom("SFX\\Radio\\scpradio0.ogg");
    BonkAchv = Achievement::Create("Bonk", false);
    BonkAchv.InsertBefore(Achievement::AchvConsole);
}

void Hook_InitializeEvents() {
    Event::Create("guardspin", "room3pit", 0, 1);
}

void Hook_UpdateEvent(CB::Event e) {
    if (e.Name == "guardspin") {
        e.Room.Objects[1].Turn(0, FPSFactor * 10, 0);
    }
}

bool Hook_FillRoom(CB::Room r) {
    if (r.Objects[1] == null) r.Objects[1] = CB::NPC::Create(NPC::Type::Guard, r.X, r.Y + 1, r.Z).Collider;
    return false;
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
