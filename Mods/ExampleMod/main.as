void Hook_InitializeEvents() {
    CreateEvent("guardspin", "room3pit", 0, 1);
}

void Hook_UpdateEvent(CB::Event@ e) {
    if (e.Name == "guardspin") {
        e.Room.Objects[1].Turn(0, FPSFactor * 10, 0);
    }
}

void Hook_FillRoom(CB::Room@ r) {
    if (r.Objects[1] == null) @r.Objects[1] = CB::NPC(NPC::Type::Guard, r.X, r.Y + 1, r.Z).Collider;
}

using namespace CB;
using namespace B3D;

float configuredFOV = -1.f;

void Hook_Update() {
    if (configuredFOV == -1.f) {
        configuredFOV = Options::FOV;
    }
    Options::FOV = configuredFOV * (1.f + Player::CurrentSpeed * 5);
}

void Hook_CombineItems(Item@ dragged, Item@ onto) {
    if (dragged.Template.Name.Substring(0, 3) != "key" || onto.Template.Name.Substring(0, 3) != "key") return;

    string lvl1 = dragged.Template.Name.Substring(3, 1);
    string lvl2 = onto.Template.Name.Substring(3, 1);

    if (lvl1.Length == 0 || lvl2.Length == 0) return;

    int res = lvl1.ParseInt() + lvl2.ParseInt();
    if (res > 6) return;

    Item@ new = Item("key" + ToString(res), Player::Camera.GetX(1), Player::Camera.GetY(1), Player::Camera.GetZ(1));
    dragged.Remove(true);
    onto.Remove(true);
}
