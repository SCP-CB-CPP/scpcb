void Hook_UpdateEvent(CB::Event@ e) {
    // Your code here.
}

void Hook_FillRoom(CB::Room@ r) {
    CB::NPC(3, r.X, r.Y + 1, r.Z);
}

using namespace CB;
using namespace B3D;

void Hook_Update() {
    NPC@ scp173 = NPC::First;
    while (scp173 != null && scp173.Type != 1) {
        @scp173 = scp173.Next;
    }
    if (scp173 != null) {
        Console::CreateMessage("Found SCP-173!");
    } else {
        Console::CreateMessage("Couldn't find SCP-173 :(");
    }
}
