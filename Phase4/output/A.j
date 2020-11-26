.class public A
.super Actor

.field b LB;
.field i I

.method public <init>(I)V
.limit stack 50
.limit locals 50
aload_0
iload_1
invokespecial Actor/<init>(I)V
return
.end method

.method public initial()V
.limit stack 50
.limit locals 50
aload_0
iconst_2
putfield A/i I
aload_0
getfield A/b LB;
aload_0
aload_0
getfield A/i I
invokevirtual B/send_foo(LActor;I)V
return
.end method

.method public setKnownActors(LB;)V
.limit stack 50
.limit locals 50
aload_0
aload_1
putfield A/b LB;
return
.end method

.method public send_bar(LActor;I)V
.limit stack 50
.limit locals 50
aload_0
new A_bar
dup
aload_0
aload_1
iload_2
invokespecial A_bar/<init>(LA;LActor;I)V
invokevirtual A/send(LMessage;)V
return
.end method

.method public bar(LActor;I)V
.limit stack 50
.limit locals 50
iconst_1
ifeq L2
iconst_0
goto L3
L2:
iconst_0
L3:
ifeq L0
getstatic java/lang/System/out Ljava/io/PrintStream;
iload_2
invokevirtual java/io/PrintStream/println(I)V
goto L1
L0: 
getstatic java/lang/System/out Ljava/io/PrintStream;
iload_2
iconst_2
iadd
invokevirtual java/io/PrintStream/println(I)V
L1: 
aload_1
aload_0
iload_2
iconst_1
iadd
invokevirtual Actor/send_foo(LActor;I)V
return
.end method
