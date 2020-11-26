.class public B
.super Actor

.field a LA;

.method public <init>(I)V
.limit stack 50
.limit locals 50
aload_0
iload_1
invokespecial Actor/<init>(I)V
return
.end method

.method public setKnownActors(LA;)V
.limit stack 50
.limit locals 50
aload_0
aload_1
putfield B/a LA;
return
.end method

.method public send_foo(LActor;I)V
.limit stack 50
.limit locals 50
aload_0
new B_foo
dup
aload_0
aload_1
iload_2
invokespecial B_foo/<init>(LB;LActor;I)V
invokevirtual B/send(LMessage;)V
return
.end method

.method public foo(LActor;I)V
.limit stack 50
.limit locals 50
aload_1
aload_0
iload_2
iconst_1
iadd
invokevirtual Actor/send_bar(LActor;I)V
return
.end method
