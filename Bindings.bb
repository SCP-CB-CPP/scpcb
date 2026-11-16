Type Bindings
    Field Key%
    Field Command$
End Type

Function CreateBinding%(key%, command$)
    For b.Bindings = Each Bindings
        If b\Key = key
            b\Command = command
            Return 1
        EndIf
    Next

    b.Bindings = New Bindings
    b\Key = key
    b\Command = command
    Return 0
End Function

Function DeleteBinding%(key%)
    For b.Bindings = Each Bindings
        If b\Key = key
            Delete b
            Return 1
        EndIf
    Next
    Return 0
End Function

Function UnbindAll()
    Delete Each Bindings
End Function

Function ProcessBindings()
    For b.Bindings = Each Bindings
        If KeyHit(b\Key)
            ExecuteConsoleCommand(b\Command, True)
        EndIf
    Next
End Function
