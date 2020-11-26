package main.visitor;
import main.ast.node.*;
import main.ast.node.Program;
import main.ast.node.declaration.*;
import main.ast.node.declaration.handler.*;
import main.ast.node.declaration.VarDeclaration;
import main.ast.node.expression.*;
import main.ast.node.expression.operators.BinaryOperator;
import main.ast.node.expression.operators.UnaryOperator;
import main.ast.node.expression.values.BooleanValue;
import main.ast.node.expression.values.IntValue;
import main.ast.node.expression.values.StringValue;
import main.ast.node.statement.*;
import java.util.*;
import java.io.File;
import java.io.FileWriter;


public class VisitorCodeGenerator implements Visitor {

    private FileWriter fileWriter;
    private FileWriter fileWriter2;
    private String actorName = "";
    private HandlerDeclaration currentHandler;
    private int labelNum = 0;
    private Stack<String> Break = new Stack<>();
    private Stack<String> Continue = new Stack<>();
    private ArrayList<String> knownActors = new ArrayList<>();
    private ArrayList<String> actorVars = new ArrayList<>();
    private ArrayList<String> handlers = new ArrayList<>();
    private ArrayList<String> actors = new ArrayList<>();
    private ArrayList<String> mains = new ArrayList<>();
    private ArrayList<String> argInitial = new ArrayList<>();
    private ArrayList<String> msgArgs = new ArrayList<>();
    private ArrayList<String> msgLocals = new ArrayList<>();
    private boolean isLeftHandSide = false;
    private boolean seen = false;
    private int counterForMain = 1;

    public int getIndex(Identifier id){
        if(currentHandler instanceof InitHandlerDeclaration){
            int i;
            for(i = 0; i < currentHandler.getArgs().size();i++){
                System.out.println("{{");
                System.out.println(currentHandler.getArgs().get(i).getIdentifier().getName() );
                if(currentHandler.getArgs().get(i).getIdentifier().getName().equals(id.getName()))
                    return i + 1;
            }
            for(i = 0; i < currentHandler.getLocalVars().size();i++){
                if(currentHandler.getLocalVars().get(i).getIdentifier().getName().equals(id.getName()))
                    return i + 1 + currentHandler.getArgs().size();
            }
        }
        else {
            int i;
            for(i = 0; i < currentHandler.getArgs().size();i++){
                System.out.println("{{");
                System.out.println(currentHandler.getArgs().get(i).getIdentifier().getName() );
                if(currentHandler.getArgs().get(i).getIdentifier().getName().equals(id.getName()))
                    return i + 2;
            }
            for(i = 0; i < currentHandler.getLocalVars().size();i++){
                if(currentHandler.getLocalVars().get(i).getIdentifier().getName().equals(id.getName()))
                    return i + 2 + currentHandler.getArgs().size();
            }
        }

        return -1;
    }

    protected void visitStatement( Statement stat )
    {
        if( stat == null )
            return;
        else if( stat instanceof MsgHandlerCall )
            this.visit( ( MsgHandlerCall ) stat );
        else if( stat instanceof Block )
            this.visit( ( Block ) stat );
        else if( stat instanceof Conditional )
            this.visit( ( Conditional ) stat );
        else if( stat instanceof For )
            this.visit( ( For ) stat );
        else if( stat instanceof Break )
            this.visit( ( Break ) stat );
        else if( stat instanceof Continue )
            this.visit( ( Continue ) stat );
        else if( stat instanceof Print )
            this.visit( ( Print ) stat );
        else if( stat instanceof Assign )
            this.visit( ( Assign ) stat );
    }

    private void fields(Program program) {
        for (ActorDeclaration actor : program.getActors()) {
            int index = 0;
            for (VarDeclaration knownActor : actor.getKnownActors()) {
                knownActors.add(actor.getName().getName() + "#"
                        + knownActor.getIdentifier().getName() + "#"
                        + knownActor.getType().toString() + "#" + index);
                index++;
            }
            for (VarDeclaration actorVar : actor.getActorVars()) {
                actorVars.add(actor.getName().getName() + "#"
                        + actorVar.getIdentifier().getName() + "#"
                        + actorVar.getType().toString() + "#" + index);
                index++;
            }
            String args = "";
            if(actor.getInitHandler() != null) {
                for (VarDeclaration arg : actor.getInitHandler().getArgs()) {
                    args += returnType(arg.getType().toString());
                }
            }
            for (HandlerDeclaration hd : actor.getMsgHandlers()) {
                for (VarDeclaration arg : hd.getArgs()) {
                    args += returnType(arg.getType().toString());
                }
                handlers.add(actor.getName().getName() + "#" + hd.getName().getName() + "#" + args);
            }
        }
    }

    protected void visitExpr( Expression expr )
    {
        if( expr == null )
            return;
        else if( expr instanceof UnaryExpression )
            this.visit( ( UnaryExpression ) expr );
        else if( expr instanceof BinaryExpression )
            this.visit( ( BinaryExpression ) expr );
        else if( expr instanceof ArrayCall )
            this.visit( ( ArrayCall ) expr );
        else if( expr instanceof ActorVarAccess )
            this.visit( ( ActorVarAccess ) expr );
        else if( expr instanceof Identifier )
            this.visit( ( Identifier ) expr );
        else if( expr instanceof Self )
            this.visit( ( Self ) expr );
        else if( expr instanceof Sender )
            this.visit( ( Sender ) expr );
        else if( expr instanceof BooleanValue )
            this.visit( ( BooleanValue ) expr );
        else if( expr instanceof IntValue )
            this.visit( ( IntValue ) expr );
        else if( expr instanceof StringValue )
            this.visit( ( StringValue ) expr );
    }

    public String returnType(String t) {
        switch (t) {
            case "int[]":
                return "[I";
            case "int":
                return "I";
            case "boolean":
                return "Ljava/lang/boolean;";
            case "string":
                return "Ljava/lang/String;";
            default:
                return "L"+t+";";
        }
    }

    private void printOut(String in){
        System.out.println("--------");
        System.out.println(in);
        System.out.println("--------");
    }

    @Override
    public void visit(Program program) {
        fields(program);
//        for(String s:actorVars){
//            printOut("ACVAR");
//            printOut(s);
//        }
//        for(String s:knownActors){
//            printOut("KNACVAR");
//            printOut(s);
//        }
//        for(String s:handlers){
//            printOut("HAN");
//            printOut(s);
//        }
        File dir = new File("output");
        dir.mkdir();
        File file = new File("output/DefaultActor.j");
        try {
            this.fileWriter2 = fileWriter;
            file.createNewFile();
            this.fileWriter = new FileWriter(file);
            this.fileWriter.write(".class public DefaultActor\n" +
                    ".super java/lang/Thread\n" +
                    "\n" +
                    ".method public <init>()V\n" +
                    ".limit stack 50\n" +
                    ".limit locals 50\n" +
                    "aload_0\n" +
                    "invokespecial java/lang/Thread/<init>()V\n" +
                    "return\n" +
                    ".end method\n\n");
            for (ActorDeclaration actorDeclaration : program.getActors()) {
                for (HandlerDeclaration hd : actorDeclaration.getMsgHandlers()) {
                    String inArgs = "";
                    for (VarDeclaration arg : hd.getArgs()) {
                        inArgs += returnType(arg.getType().toString());///////FASELE
                    }
                    this.fileWriter.write(".method public send_" + hd.getName().getName() + "(LActor;" + inArgs + ")V\n" +
                            ".limit stack 50\n" +
                            ".limit locals 50\n" +
                            "getstatic java/lang/System/out Ljava/io/PrintStream;\n" +
                            "ldc \"there is no msghandler named " + hd.getName().getName() +  " in sender\"\n" +
                            "invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V\n" +
                            "return\n" +
                            ".end method\n\n");
                }
            }
            this.fileWriter.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        this.fileWriter = fileWriter2;
        for(ActorDeclaration ad : program.getActors()) {
            actorName = ad.getName().getName();
            actors.add(ad.getName().getName()+"#"+ad.getQueueSize()+"#"+ad.getInitHandler());
            ad.accept(this);
        }
        program.getMain().accept(this);
    }

    @Override
    public void visit(ActorDeclaration actorDeclaration) {//check
        if(actorDeclaration.getInitHandler() != null) {
            currentHandler = actorDeclaration.getInitHandler();
            for (VarDeclaration var : actorDeclaration.getInitHandler().getArgs()) {
                msgArgs.add(actorName + "#" + var.getType().toString() + "#"
                        + actorDeclaration.getInitHandler().getName().getName() + "#" + var.getIdentifier().getName());
            }
            for (VarDeclaration var : actorDeclaration.getInitHandler().getLocalVars()) {
                msgLocals.add(actorName + "#" + var.getType().toString() + "#"
                        + actorDeclaration.getInitHandler().getName().getName() + var.getIdentifier().getName());
            }
        }
        for (HandlerDeclaration hd : actorDeclaration.getMsgHandlers()) {
            currentHandler = hd;
            for (VarDeclaration var : hd.getArgs()) {
                msgArgs.add(actorName + "#" + var.getType().toString() + "#"
                        + hd.getName().getName() + "#" + var.getIdentifier().getName());
            }
            for (VarDeclaration var : hd.getLocalVars()) {
                msgLocals.add(actorName + "#" + var.getType().toString() + "#"
                        + hd.getName().getName() + var.getIdentifier().getName());
            }
        }
        File file = new File("output/"+actorName + ".j");
        try {
            file.createNewFile();
            this.fileWriter = new FileWriter(file);
            this.fileWriter.write(".class public "+actorName+"\n");
            this.fileWriter.write(".super Actor\n");
            this.fileWriter.write("\n");
            for(VarDeclaration vd:actorDeclaration.getKnownActors()) {
                this.fileWriter.write(".field " +vd.getIdentifier().getName()+" "+returnType(vd.getType().toString())+"\n");
            }
            for(VarDeclaration vd:actorDeclaration.getActorVars()) {
                this.fileWriter.write(".field " +vd.getIdentifier().getName()+" "+returnType(vd.getType().toString())+"\n");
            }
            this.fileWriter.write("\n");
            this.fileWriter.write(".method public <init>(I)V\n");
            this.fileWriter.write(".limit stack 50\n");
            this.fileWriter.write(".limit locals 50\n");
            this.fileWriter.write("aload_0\n");
            this.fileWriter.write("iload_1\n");
            this.fileWriter.write("invokespecial Actor/<init>(I)V\n");
            this.fileWriter.write("return\n");
            this.fileWriter.write(".end method\n");
            this.fileWriter.write("\n");
            if(actorDeclaration.getInitHandler() != null) {
                actorDeclaration.getInitHandler().accept(this);
                String inArgs = "";
                for (VarDeclaration arg : actorDeclaration.getInitHandler().getArgs()) {
                    inArgs += returnType(arg.getType().toString());///////FASELE
                }
                this.fileWriter.write(".method public initial("+inArgs+")V\n");
                this.fileWriter.write(".limit stack 50\n");
                this.fileWriter.write(".limit locals 50\n");
                this.fileWriter.write("aload_0\n");
                for(Statement s : actorDeclaration.getInitHandler().getBody()){
                    s.accept(this);
                }
                this.fileWriter.write("return\n");
                this.fileWriter.write(".end method\n");
                this.fileWriter.write("\n");
            }
            for(VarDeclaration vd:actorDeclaration.getKnownActors()) {
                this.fileWriter.write(".method public setKnownActors("+returnType(vd.getType().toString())+")V\n");
                this.fileWriter.write(".limit stack 50\n");
                this.fileWriter.write(".limit locals 50\n");
                this.fileWriter.write("aload_0\n");
                this.fileWriter.write("aload_1\n");
                this.fileWriter.write("putfield "+actorName+"/"+vd.getIdentifier().getName()+" "+returnType(vd.getType().toString())+"\n");
                this.fileWriter.write("return\n");
                this.fileWriter.write(".end method\n\n");
            }
            for (HandlerDeclaration hd : actorDeclaration.getMsgHandlers()) {
                hd.accept(this);
            }
            this.fileWriter.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        //
    }

    @Override
    public void visit(HandlerDeclaration handlerDeclaration) {//CHECK
        currentHandler = handlerDeclaration;
        String handlerName = handlerDeclaration.getName().getName();
        if(!handlerDeclaration.getName().getName().equals("initial")) {
            File file = new File("output/" + actorName + "_" + handlerName + ".j");
            try {
                this.fileWriter2 = fileWriter;
                file.createNewFile();
                this.fileWriter = new FileWriter(file);
                this.fileWriter.write(".class public " + actorName + "_" + handlerName + "\n");
                this.fileWriter.write(".super Message\n");
                this.fileWriter.write("\n");
                for (VarDeclaration arg : handlerDeclaration.getArgs()) {
                    this.fileWriter.write(".field private "+arg.getIdentifier().getName() + " " + returnType(arg.getType().toString()));
                    this.fileWriter.write("\n");
                }
                this.fileWriter.write(".field private receiver L"+actorName+";\n");
                this.fileWriter.write(".field private sender LActor;\n");
                this.fileWriter.write("\n");
                String inArgs = "";
                for (VarDeclaration arg : handlerDeclaration.getArgs()) {
                    inArgs += returnType(arg.getType().toString());///////FASELE
                }
                this.fileWriter.write(".method public <init>(L"+actorName+";"+"LActor;"+inArgs+")V\n");
                this.fileWriter.write(".limit stack 50\n");
                this.fileWriter.write(".limit locals 50\n");
                ///LOAD BAGHIYE MOTAGHAYER HA
                this.fileWriter.write("aload_0\n");
                this.fileWriter.write("invokespecial Message/<init>()V\n");
                this.fileWriter.write("aload_0\n");
                this.fileWriter.write("aload_1\n");
                this.fileWriter.write("putfield "+ actorName + "_" + handlerName+"/receiver L"+actorName+";\n");
                this.fileWriter.write("aload_0\n");
                this.fileWriter.write("aload_2\n");
                this.fileWriter.write("putfield "+ actorName + "_" + handlerName+"/sender LActor;\n");
                for(int i = 0; i < handlerDeclaration.getArgs().size(); i++){
                    this.fileWriter.write("aload_0\n");/////int[]
                    if(handlerDeclaration.getArgs().get(i).getType().toString().equals("int")){
                        if(i == 0)
                            this.fileWriter.write("iload_3\n");
                        else {
                            int num = 3+i;
                            this.fileWriter.write("iload " + num + "\n");
                        }
                    }
                    else{
                        if(i == 0)
                            this.fileWriter.write("aload_3\n");
                        else {
                            int num = 3+i;
                            this.fileWriter.write("aload " + num + "\n");
                        }
                    }
                    this.fileWriter.write("putfield "+ actorName + "_" + handlerName+"/"+ handlerDeclaration.getArgs().get(i).getIdentifier().getName()+" "+returnType(handlerDeclaration.getArgs().get(i).getType().toString())+"\n");
                }
                this.fileWriter.write("return\n");
                this.fileWriter.write(".end method\n");
                this.fileWriter.write("\n");
                this.fileWriter.write(".method public execute()V\n");
                this.fileWriter.write(".limit stack 50\n");
                this.fileWriter.write(".limit locals 50\n");
                //NEMIKHAD????
                this.fileWriter.write("aload_0\n");
                this.fileWriter.write("getfield "+ actorName + "_" + handlerName+"/receiver L"+actorName+";\n");
                this.fileWriter.write("aload_0\n");
                this.fileWriter.write("getfield "+ actorName + "_" + handlerName+"/sender LActor;\n");
                for(int i = 0; i < handlerDeclaration.getArgs().size(); i++){
                    this.fileWriter.write("aload_0\n");/////int[]
                    this.fileWriter.write("getfield "+ actorName + "_" + handlerName+"/"+ handlerDeclaration.getArgs().get(i).getIdentifier().getName()+" "+returnType(handlerDeclaration.getArgs().get(i).getType().toString())+"\n");
                }
                this.fileWriter.write("invokevirtual "+ actorName + "/" + handlerName+"(LActor;"+inArgs+")V\n");
                this.fileWriter.write("return\n");
                this.fileWriter.write(".end method\n");
                this.fileWriter.close();
//                for (Statement s : handlerDeclaration.getBody()) {
//                    s.accept(this);
//                }
                //this.fileWriter.close();
            } catch (Exception e) {
                System.out.println(e);
            }
            ////ToDo:Understand
            this.fileWriter = fileWriter2;
            try {
                String inArgs = "";
                for (VarDeclaration arg : handlerDeclaration.getArgs()) {
                    inArgs += returnType(arg.getType().toString());///////FASELE
                }
                this.fileWriter.write(".method public send_"+handlerName+"(LActor;"+inArgs+")V\n");
                this.fileWriter.write(".limit stack 50\n" + ".limit locals 50\n");
                this.fileWriter.write("aload_0\n");
                this.fileWriter.write("new "+actorName+"_"+handlerName+"\n");
                this.fileWriter.write("dup\n");
                this.fileWriter.write("aload_0\n");
                this.fileWriter.write("aload_1\n");
                for (VarDeclaration arg : handlerDeclaration.getArgs()) {
                    if(arg.getType().toString().equals("int")||arg.getType().toString().equals("boolean"))
                        this.fileWriter.write("iload_"+getIndex(arg.getIdentifier())+"\n");
                    else if(arg.getType().toString().equals("int[]"))
                        this.fileWriter.write("iaload_"+getIndex(arg.getIdentifier())+"\n");
                    else
                        this.fileWriter.write("aload_"+getIndex(arg.getIdentifier())+"\n");
                }
                this.fileWriter.write("invokespecial "+actorName + "_" + handlerDeclaration.getName().getName()
                        +"/<init>(L"+actorName+";"+"LActor;"+inArgs+")V\n");
                this.fileWriter.write("invokevirtual "+actorName+"/send(LMessage;)V\n");
                this.fileWriter.write("return\n" + ".end method\n\n");
                this.fileWriter.write(".method public "+handlerName+"(LActor;"+inArgs+")V\n");
                this.fileWriter.write(".limit stack 50\n");
                this.fileWriter.write(".limit locals 50\n");
                for(Statement s:handlerDeclaration.getBody()){
                    s.accept(this);
                }
                //
                this.fileWriter.write("return\n");
                this.fileWriter.write(".end method\n");
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    @Override
    public void visit(VarDeclaration Declaration) {
        //HICHI
    }

    @Override
    public void visit(Main mainActors) {
        for (ActorInstantiation ai : mainActors.getMainActors()) {
            mains.add(ai.getIdentifier().getName() + "#" + ai.getType().toString() + "#" + counterForMain);
            counterForMain ++;
        }
        for (ActorInstantiation ai : mainActors.getMainActors()) {
            for(Expression arg : ai.getInitArgs()){
                argInitial.add(arg+"#"+arg.getType().toString()+"#"+counterForMain);
                counterForMain++;
            }
        }
        File file = new File("output/Main.j");
        try {
            file.createNewFile();
            this.fileWriter = new FileWriter(file);
            this.fileWriter.write(".class public Main\n");
            this.fileWriter.write(".super java/lang/Object\n");
            this.fileWriter.write("\n");
            this.fileWriter.write(".method public <init>()V\n");
            this.fileWriter.write(".limit stack 50\n");
            this.fileWriter.write(".limit locals 50\n");
            this.fileWriter.write("aload_0\n");
            this.fileWriter.write("invokespecial java/lang/Object/<init>()V\n");
            this.fileWriter.write("return\n");
            this.fileWriter.write(".end method\n");
            this.fileWriter.write("\n");
            this.fileWriter.write(".method public static main([Ljava/lang/String;)V\n");
            this.fileWriter.write(".limit stack 50\n");
            this.fileWriter.write(".limit locals 50\n");
            for (ActorInstantiation ai : mainActors.getMainActors()) {
                ai.accept(this);
            }
            String inArgs = "";
            for(ActorInstantiation ai : mainActors.getMainActors()){
                for (String s : mains) {
                    if(s.split("#", 3)[0].equals(ai.getIdentifier().getName())) {
                        this.fileWriter.write("aload_"+s.split("#", 3)[2]+"\n");
                    }
                }
                for (Identifier arg : ai.getKnownActors()) {
                    for (String s1 : mains) {
                        if(s1.split("#", 3)[0].equals(arg.getName())) {
                            this.fileWriter.write("aload_"+s1.split("#", 3)[2]+"\n");
                            break;
                        }
                    }
                    this.fileWriter.write("invokevirtual "+ai.getType().toString()
                            +"/setKnownActors("+returnType(arg.getType().toString())+")V\n");
                }
            }
            for(ActorInstantiation ai : mainActors.getMainActors()){
                for (String s : mains){//Initial Dare Ya na?
                    if(s.split("#", 3)[0].equals(ai.getIdentifier().getName())) {
                        for(String s1 : actors) {
                            if (s.split("#", 3)[1].equals(s1.split("#", 3)[0])){
                                if(!s1.split("#", 3)[2].equals("null")){
                                    this.fileWriter.write("aload_"+s.split("#", 3)[2]+"\n");
                                    for(Expression arg : ai.getInitArgs()){
                                        inArgs += returnType(arg.getType().toString());
                                        for (String s2 : argInitial) {
                                            if(s2.split("#", 3)[0].equals(arg.toString())) {
                                                if(s2.split("#", 3)[1].equals("int")||s2.split("#", 3)[1].equals("boolean"))
                                                    this.fileWriter.write("iload_"+s2.split("#", 3)[2]+"\n");
                                                else if(s2.split("#", 3)[1].equals("int[]"))
                                                    this.fileWriter.write("iaload_"+s2.split("#", 3)[2]+"\n");
                                                else
                                                    this.fileWriter.write("aload_"+s2.split("#", 3)[2]+"\n");
                                                break;
                                            }
                                        }
                                    }
                                    this.fileWriter.write("invokevirtual "+s.split("#", 3)[1]
                                            +"/initial("+inArgs+")V\n");
                                }
                            }
                        }
                    }
                }
            }
            for(ActorInstantiation ai : mainActors.getMainActors()) {
                for (String s : mains) {
                    if (s.split("#", 3)[0].equals(ai.getIdentifier().getName())) {
                        this.fileWriter.write("aload_" + s.split("#", 3)[2] + "\n");
                        this.fileWriter.write("invokevirtual "+ai.getType().toString()
                                +"/start()V\n");
                    }
                }
            }
            this.fileWriter.write("return\n");
            this.fileWriter.write(".end method\n");
            this.fileWriter.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void visit(ActorInstantiation actorInstantiation) {
        try {
            this.fileWriter.write("new "+actorInstantiation.getType().toString()+"\n");
            this.fileWriter.write("dup\n");
            for (String s : actors) {
                if (s.split("#", 3)[0].equals(actorInstantiation.getType().toString())) {
                    this.fileWriter.write("iconst_" + s.split("#", 3)[1]+"\n");
                }
            }
            this.fileWriter.write("invokespecial "+actorInstantiation.getType()
                    .toString()+"/<init>(I)V\n");
            for (String s : mains) {
                if(s.split("#", 3)[1].equals(actorInstantiation.getType().toString())) {
                    this.fileWriter.write("astore_"+s.split("#", 3)[2]+"\n");
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        if(unaryExpression.getUnaryOperator() == UnaryOperator.minus) {
            try {
                unaryExpression.getOperand().accept(this);
                this.fileWriter.write("ineg\n");
            } catch(Exception e){
                System.out.println(e);
            }
        }
        if(unaryExpression.getUnaryOperator() == UnaryOperator.postdec) {
            ///////////////////////////////////////////////////////////////////////////////
            try {
                boolean isSeen = false;
                unaryExpression.getOperand().accept(this);
                if(unaryExpression.getOperand() instanceof Identifier) {
                    for (String s : knownActors) {
                        if (s.split("#", 4)[0].equals(actorName) &&
                                s.split("#", 4)[1].equals(((Identifier) unaryExpression.getOperand()).getName())) {
                            isSeen = true;
                            this.fileWriter.write("putfield " + actorName
                                    + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                            /* else  if(!isLeftHandSide) {
                                this.fileWriter.write("getfield " + actorName
                                        + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                            }*/
                        }
                    }
                    for (String s : actorVars) {
                        if (s.split("#", 4)[0].equals(actorName) &&
                                s.split("#", 4)[1].equals(((Identifier) unaryExpression.getOperand()).getName())) {
                            isSeen = true;
                            this.fileWriter.write("putfield " + actorName
                                    + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                            /*else if(!isLeftHandSide) {
                                this.fileWriter.write("getfield " + actorName
                                        + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                            }*/
                        }
                    }
                    if(!isSeen) {
                        if(isLeftHandSide) {
                            this.fileWriter.write("istore_" + 1 + "\n");
                        }
                    }
                    this.fileWriter.write("iconst_1\n" + "isub\n");
                    for (String s : knownActors) {
                        if (s.split("#", 4)[0].equals(actorName) &&
                                s.split("#", 4)[1].equals(((Identifier) unaryExpression.getOperand()).getName())) {
                            isSeen = true;
                            this.fileWriter.write("getfield " + actorName
                                    + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                        }
                    }
                    for (String s : actorVars) {
                        if (s.split("#", 4)[0].equals(actorName) &&
                                s.split("#", 4)[1].equals(((Identifier) unaryExpression.getOperand()).getName())) {
                            isSeen = true;
                            this.fileWriter.write("getfield " + actorName
                                    + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                        }
                    }
                    if(!isSeen) {
                        if(isLeftHandSide) {
                            this.fileWriter.write("load_" + 1 + "\n");
                        }
                    }
                }
            } catch(Exception e){
                System.out.println(e);
            }
            ///////////////////////////////////////////////////////////////////////////////
        }
        if( unaryExpression.getUnaryOperator() == UnaryOperator.postinc){
            try {
                boolean isSeen = false;
                unaryExpression.getOperand().accept(this);
                if(unaryExpression.getOperand() instanceof Identifier) {
                    for (String s : knownActors) {
                        if (s.split("#", 4)[0].equals(actorName) &&
                                s.split("#", 4)[1].equals(((Identifier) unaryExpression.getOperand()).getName())) {
                            isSeen = true;
                            this.fileWriter.write("getfield " + actorName
                                    + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                        }
                    }
                    for (String s : actorVars) {
                        if (s.split("#", 4)[0].equals(actorName) &&
                                s.split("#", 4)[1].equals(((Identifier) unaryExpression.getOperand()).getName())) {
                            isSeen = true;
                            this.fileWriter.write("getfield " + actorName
                                    + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                        }
                    }
                    if(!isSeen) {
                        if(isLeftHandSide) {
                            this.fileWriter.write("load_" + 1 + "\n");
                        }
                    }
                    this.fileWriter.write("iconst_1\n" + "iadd\n");

                    for (String s : knownActors) {
                        if (s.split("#", 4)[0].equals(actorName) &&
                                s.split("#", 4)[1].equals(((Identifier) unaryExpression.getOperand()).getName())) {
                            isSeen = true;
                            this.fileWriter.write("putfield " + actorName
                                    + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                            /* else  if(!isLeftHandSide) {
                                this.fileWriter.write("getfield " + actorName
                                        + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                            }*/
                        }
                    }
                    for (String s : actorVars) {
                        if (s.split("#", 4)[0].equals(actorName) &&
                                s.split("#", 4)[1].equals(((Identifier) unaryExpression.getOperand()).getName())) {
                            isSeen = true;
                            this.fileWriter.write("putfield " + actorName
                                    + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                            /*else if(!isLeftHandSide) {
                                this.fileWriter.write("getfield " + actorName
                                        + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                            }*/
                        }
                    }
                    if(!isSeen) {
                        if(isLeftHandSide) {
                            this.fileWriter.write("istore_" + 1 + "\n");
                        }
                    }
                }
            } catch(Exception e){
                System.out.println(e);
            }
        }
        if(unaryExpression.getUnaryOperator() == UnaryOperator.predec){
            try {
                unaryExpression.getOperand().accept(this);
                boolean isSeen = false;
                if(unaryExpression.getOperand() instanceof Identifier) {
                    for (String s : knownActors) {
                        if (s.split("#", 4)[0].equals(actorName) &&
                                s.split("#", 4)[1].equals(((Identifier) unaryExpression.getOperand()).getName())) {
                            isSeen = true;
                            this.fileWriter.write("putfield " + actorName
                                    + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                            /* else  if(!isLeftHandSide) {
                                this.fileWriter.write("getfield " + actorName
                                        + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                            }*/
                        }
                    }
                    for (String s : actorVars) {
                        if (s.split("#", 4)[0].equals(actorName) &&
                                s.split("#", 4)[1].equals(((Identifier) unaryExpression.getOperand()).getName())) {
                            isSeen = true;
                            this.fileWriter.write("putfield " + actorName
                                    + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                            /*else if(!isLeftHandSide) {
                                this.fileWriter.write("getfield " + actorName
                                        + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                            }*/
                        }
                    }
                    if(!isSeen) {
                        if(isLeftHandSide) {
                            this.fileWriter.write("istore_" + 1 + "\n");
                        }
                    }
                    this.fileWriter.write("iconst_1\n" + "isub\n");
                    for (String s : knownActors) {
                        if (s.split("#", 4)[0].equals(actorName) &&
                                s.split("#", 4)[1].equals(((Identifier) unaryExpression.getOperand()).getName())) {
                            isSeen = true;
                            this.fileWriter.write("getfield " + actorName
                                    + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                        }
                    }
                    for (String s : actorVars) {
                        if (s.split("#", 4)[0].equals(actorName) &&
                                s.split("#", 4)[1].equals(((Identifier) unaryExpression.getOperand()).getName())) {
                            isSeen = true;
                            this.fileWriter.write("getfield " + actorName
                                    + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                        }
                    }
                    if(!isSeen) {
                        if(isLeftHandSide) {
                            this.fileWriter.write("load_" + 1 + "\n");
                        }
                    }

                }
            } catch(Exception e){
                System.out.println(e);
            }
        }
        if(unaryExpression.getUnaryOperator() == UnaryOperator.preinc){
            try {
                unaryExpression.getOperand().accept(this);
                boolean isSeen = false;
                if(unaryExpression.getOperand() instanceof Identifier) {
                    for (String s : knownActors) {
                        if (s.split("#", 4)[0].equals(actorName) &&
                                s.split("#", 4)[1].equals(((Identifier) unaryExpression.getOperand()).getName())) {
                            isSeen = true;
                            this.fileWriter.write("putfield " + actorName
                                    + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                            /* else  if(!isLeftHandSide) {
                                this.fileWriter.write("getfield " + actorName
                                        + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                            }*/
                        }
                    }
                    for (String s : actorVars) {
                        if (s.split("#", 4)[0].equals(actorName) &&
                                s.split("#", 4)[1].equals(((Identifier) unaryExpression.getOperand()).getName())) {
                            isSeen = true;
                            this.fileWriter.write("putfield " + actorName
                                    + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                            /*else if(!isLeftHandSide) {
                                this.fileWriter.write("getfield " + actorName
                                        + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                            }*/
                        }
                    }
                    if(!isSeen) {
                        if(isLeftHandSide) {
                            this.fileWriter.write("istore_" + 1 + "\n");
                        }
                    }
                    this.fileWriter.write("iconst_1\n" + "iadd\n");
                    for (String s : knownActors) {
                        if (s.split("#", 4)[0].equals(actorName) &&
                                s.split("#", 4)[1].equals(((Identifier) unaryExpression.getOperand()).getName())) {
                            isSeen = true;
                            this.fileWriter.write("getfield " + actorName
                                    + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                        }
                    }
                    for (String s : actorVars) {
                        if (s.split("#", 4)[0].equals(actorName) &&
                                s.split("#", 4)[1].equals(((Identifier) unaryExpression.getOperand()).getName())) {
                            isSeen = true;
                            this.fileWriter.write("getfield " + actorName
                                    + "/" + ((Identifier) unaryExpression.getOperand()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                        }
                    }
                    if(!isSeen) {
                        if(isLeftHandSide) {
                            this.fileWriter.write("load_" + 1 + "\n");
                        }
                    }
                }
            } catch(Exception e){
                System.out.println(e);
            }
        }
        if(unaryExpression.getUnaryOperator() == UnaryOperator.not) {
            try {
                unaryExpression.getOperand().accept(this);
                int currentLabelNum = labelNum++;
                int nextLabelNum = labelNum ++;
                this.fileWriter.write("ifeq L"+currentLabelNum+"\n");
                this.fileWriter.write("iconst_1\n");
                this.fileWriter.write("goto L"+nextLabelNum+"\n");
                this.fileWriter.write("L"+currentLabelNum+":\n"+"iconst_0\n");
                this.fileWriter.write("L"+nextLabelNum+":\n");
            } catch (Exception e) {
                System.out.println(e);
            }
        }

    }

    @Override
    public void visit(BinaryExpression binaryExpression) {
        //ToDo:check asgn & neq
//        System.out.println(binaryExpression.getLine());
        try {
            if (binaryExpression.getBinaryOperator() == BinaryOperator.eq) {
                int currentLabelNum = labelNum++;
                int nextLabelNum = labelNum ++;
                binaryExpression.getLeft().accept(this);
                binaryExpression.getRight().accept(this);
                if(binaryExpression.getLeft().getType().toString().equals("int") || binaryExpression.getLeft().getType().toString().equals("boolean")){
                    this.fileWriter.write("if_icmpne L"+nextLabelNum+"\n");
                    this.fileWriter.write("iconst_1");
                    this.fileWriter.write("goto L"+currentLabelNum+"\n");
                    this.fileWriter.write("L"+nextLabelNum+":\n"+"iconst_0\n");
                    this.fileWriter.write("L"+currentLabelNum+":\n");
                }
                else if(binaryExpression.getLeft().getType().toString().equals("string")){//SHAK!!!
                    this.fileWriter.write("invokevirtual java/lang/Object.equals(Ljava/lang/Object;)Z");
                }
                else if(binaryExpression.getLeft().getType().toString().equals("int[]")){//SHAK!!!
                    this.fileWriter.write("invokevirtual java/util/arrays.equals([I[I)Z");
                }
                else
                    this.fileWriter.write("invokevirtual java/lang/Object.equals(Ljava/lang/Object;)Z");
            }
            if (binaryExpression.getBinaryOperator() == BinaryOperator.neq) {
                BinaryExpression be = new BinaryExpression(binaryExpression.getLeft(), binaryExpression.getRight(), BinaryOperator.eq);
                be.accept(this);
                UnaryExpression ue = new UnaryExpression(UnaryOperator.not, be);
                ue.accept(this);
            }
            if (binaryExpression.getBinaryOperator() == BinaryOperator.assign) {
                boolean iisSeen = false;
                if (binaryExpression.getRight() instanceof Identifier) {
                    for (String s : msgArgs) {
                        if (s.split("#", 4)[0].equals(actorName)
                                && s.split("#", 4)[3].equals(((Identifier) binaryExpression.getRight()).getName())) {
                            if (s.split("#", 4)[1].equals("int") || s.split("#", 4)[1].equals("boolean")) {
                                iisSeen = true;
                                this.fileWriter.write("iload_" + s.split("#", 4)[2]);
                            } else if (s.split("#", 4)[1].equals("int[]")) {
                                this.fileWriter.write("iaload");
                            } else {
                                this.fileWriter.write("aload_" + s.split("#", 4)[2]);
                            }
                        }
                    }
                    for (String s : msgLocals) {
                        if (s.split("#", 4)[0].equals(actorName)
                                && s.split("#", 4)[3].equals(((Identifier) binaryExpression.getRight()).getName())) {
                            if (s.split("#", 4)[1].equals("int") || s.split("#", 4)[1].equals("boolean")) {
                                iisSeen = true;
                                this.fileWriter.write("iload_" + s.split("#", 4)[2]);
                            } else if (s.split("#", 4)[1].equals("int[]")) {
                                this.fileWriter.write("iaload");
                            } else {
                                this.fileWriter.write("aload_" + s.split("#", 4)[2]);
                            }
                        }
                    }
                }
                if (!iisSeen) {
                    binaryExpression.getRight().accept(this);
                }
                seen = false;
                isLeftHandSide = true;
                iisSeen = false;
                if (binaryExpression.getRight() instanceof Identifier) {
                    for (String s : msgArgs) {
                        if (s.split("#", 4)[0].equals(actorName)
                                && s.split("#", 4)[3].equals(((Identifier) binaryExpression.getLeft()).getName())) {
                            if (s.split("#", 4)[1].equals("int") || s.split("#", 4)[1].equals("boolean")) {
                                iisSeen = true;
                                this.fileWriter.write("iload_" + s.split("#", 4)[2]);
                            } else if (s.split("#", 4)[1].equals("int[]")) {
                                this.fileWriter.write("iaload");
                            } else {
                                this.fileWriter.write("aload_" + s.split("#", 4)[2]);
                            }
                        }
                    }
                    for (String s : msgLocals) {
                        if (s.split("#", 4)[0].equals(actorName)
                                && s.split("#", 4)[3].equals(((Identifier) binaryExpression.getLeft()).getName())) {
                            if (s.split("#", 4)[1].equals("int") || s.split("#", 4)[1].equals("boolean")) {
                                iisSeen = true;
                                this.fileWriter.write("istore_" + s.split("#", 4)[2]);
                            } else if (s.split("#", 4)[1].equals("int[]")) {
                                this.fileWriter.write("iastore");
                            } else {
                                this.fileWriter.write("astore_" + s.split("#", 4)[2]);
                            }
                        }
                    }
                }
                if (!iisSeen) {
                    binaryExpression.getLeft().accept(this);
                }
                isLeftHandSide = false;
            }
                if (binaryExpression.getBinaryOperator() == BinaryOperator.and) {
                //p&&q == (p)?q:false
                int currentLabelNum = labelNum++;
                int nextLabelNum = labelNum ++;
                binaryExpression.getLeft().accept(this);
                this.fileWriter.write("ifeq L"+currentLabelNum+"\n");
                binaryExpression.getRight().accept(this);
                this.fileWriter.write("goto L"+nextLabelNum+"\n");
                this.fileWriter.write("L"+currentLabelNum+":\n"+"iconst_0\n");
                this.fileWriter.write("L"+nextLabelNum+":\n");
            }
            if (binaryExpression.getBinaryOperator() == BinaryOperator.or) {
                //p||q == (p)?true:q
                int currentLabelNum = labelNum++;
                int nextLabelNum = labelNum ++;
                binaryExpression.getLeft().accept(this);
                this.fileWriter.write("ifeq L"+currentLabelNum+"\n");
                this.fileWriter.write("iconst_1\n");
                this.fileWriter.write("goto L"+nextLabelNum+"\n");
                this.fileWriter.write("L"+currentLabelNum+":\n");
                binaryExpression.getRight().accept(this);
                //this.fileWriter.write("\n");
                this.fileWriter.write("L"+nextLabelNum+":\n");
            }
            if (binaryExpression.getBinaryOperator() == BinaryOperator.mult) {
                binaryExpression.getLeft().accept(this);
                binaryExpression.getRight().accept(this);
                this.fileWriter.write("imul\n");
            }
            if (binaryExpression.getBinaryOperator() == BinaryOperator.div) {
                binaryExpression.getLeft().accept(this);
                binaryExpression.getRight().accept(this);
                this.fileWriter.write("idiv\n");
            }
            if (binaryExpression.getBinaryOperator() == BinaryOperator.add) {
//                System.out.println(binaryExpression.getLine());
                binaryExpression.getLeft().accept(this);
                binaryExpression.getRight().accept(this);
                this.fileWriter.write("iadd\n");
            }
            if (binaryExpression.getBinaryOperator() == BinaryOperator.sub) {
                binaryExpression.getLeft().accept(this);
                binaryExpression.getRight().accept(this);
                this.fileWriter.write("isub\n");
            }
            if (binaryExpression.getBinaryOperator() == BinaryOperator.mod) {
                binaryExpression.getLeft().accept(this);
                binaryExpression.getRight().accept(this);
                this.fileWriter.write("irem\n");
            }
            if (binaryExpression.getBinaryOperator() == BinaryOperator.gt) {
                int currentLabelNum = labelNum++;
                int nextLabelNum = labelNum ++;
                binaryExpression.getLeft().accept(this);
                binaryExpression.getRight().accept(this);
                this.fileWriter.write("if_icmple L"+currentLabelNum+"\n");
                this.fileWriter.write("iconst_1\n");
                this.fileWriter.write("goto L"+nextLabelNum+"\n");
                this.fileWriter.write("L"+currentLabelNum+":\n"+"iconst_0\n");
                this.fileWriter.write("L"+nextLabelNum+":\n");
            }
            if (binaryExpression.getBinaryOperator() == BinaryOperator.lt) {
                int currentLabelNum = labelNum++;
                int nextLabelNum = labelNum ++;
                binaryExpression.getLeft().accept(this);
                binaryExpression.getRight().accept(this);
                this.fileWriter.write("if_icmpge L"+currentLabelNum+"\n");
                this.fileWriter.write("iconst_1\n");
                this.fileWriter.write("goto L"+nextLabelNum+"\n");
                this.fileWriter.write("L"+currentLabelNum+":\n"+"iconst_0\n");
                this.fileWriter.write("L"+nextLabelNum+":\n");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
//        System.out.println(binaryExpression.getLine());
    }

    @Override
    public void visit(ArrayCall arrayCall) {
        try {
            if (!isLeftHandSide) {
                this.fileWriter.write("iaload\n");
            }
            else{
                isLeftHandSide = false;
                arrayCall.getArrayInstance().accept(this);
                arrayCall.getIndex().accept(this);
                isLeftHandSide = true;
            }
        }catch(Exception e){
            System.out.println(e);
        }
    }

    @Override
    public void visit(ActorVarAccess actorVarAccess) {
        try {
            this.fileWriter.write("aload_0\n");
            for (String s : actorVars) {
                if(s.split("#", 4).equals(actorName)
                        && s.split("#", 4).equals(actorVarAccess.getVariable())) {
                    if(isLeftHandSide) {
                        this.fileWriter.write("putfield " + actorName
                                + "/" + actorVarAccess.getVariable().getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                    }
                    else {
                        this.fileWriter.write("getfield " + actorName
                                + "/" + actorVarAccess.getVariable().getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                    }
                }
            }
        } catch(Exception e){
            System.out.println(e);
        }
    }

    @Override
    public void visit(Identifier identifier) {
//        System.out.println(identifier.getLine());
        /*for (String s : actorVars) {
            printOut(s);
        }
        printOut("**");
        printOut(actorName);*/
        try {
            boolean iisSeen = false;
//                System.out.println(msgHandlerCall.getLine());
            for (String s : msgArgs) {
                System.out.println(s);
                System.out.println(":::");
                if(s.split("#", 4)[3].equals(identifier.getName())
                        && currentHandler.getName().getName().equals(s.split("#", 4)[2])) {
                    printOut(s);
                    if(s.split("#", 4)[1].equals("int") || s.split("#", 4)[1].equals("boolean")) {
                        iisSeen = true;
                        this.fileWriter.write("iload_" + getIndex(identifier) + "\n");
                    }
                    else if (s.split("#", 4)[1].equals("int[]")) {
                        this.fileWriter.write("iaload\n");
                    }
                    else {
                        this.fileWriter.write("aload_" + getIndex(identifier) + "\n");
                    }
                }
            }
            for (String s : msgLocals) {
                if(s.split("#", 4)[3].equals(identifier.getName())
                        && currentHandler.getName().getName().equals(s.split("#", 4)[2])) {
                    if(s.split("#", 4)[1].equals("int") || s.split("#", 4)[1].equals("boolean")) {
                        iisSeen = true;
                        this.fileWriter.write("iload_" + s.split("#", 4)[2]);
                    }
                    else if (s.split("#", 4)[1].equals("int[]")) {
                        this.fileWriter.write("iaload");
                    }
                    else {
                        this.fileWriter.write("aload_" + s.split("#", 4)[2]);
                    }
                }
            }
            if(!iisSeen) {
                for (String s : knownActors) {
                    if (s.split("#", 4)[0].equals(actorName) &&
                            s.split("#", 4)[1].equals(identifier.getName())) {
                        seen = true;
                        if(isLeftHandSide) {
//                            this.fileWriter.write("aload_0\n");
                            this.fileWriter.write("putfield " + actorName
                                    + "/" + identifier.getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                        }
                        else {
                            this.fileWriter.write("aload_0\n");
                            this.fileWriter.write("getfield " + actorName
                                    + "/" + identifier.getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                        }
                    }
                }
                for (String s : actorVars) {
                    if (s.split("#", 4)[0].equals(actorName) &&
                            s.split("#", 4)[1].equals(identifier.getName())) {
                        seen = true;
                        if(isLeftHandSide) {
//                            this.fileWriter.write("aload_0\n");
                            this.fileWriter.write("putfield " + actorName
                                    + "/" + identifier.getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                        }
                        else {
                            this.fileWriter.write("aload_0\n");
                            this.fileWriter.write("getfield " + actorName
                                    + "/" + identifier.getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                        }
                    }
                }
            }
        } catch(Exception e){
            System.out.println(e);
        }
//        System.out.println(identifier.getLine());
    }

    @Override
    public void visit(Self self) {
        try {
            this.fileWriter.write("aload_0\n");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void visit(Sender sender) {//msg handler arg avval
        try {
            this.fileWriter.write("aload_1\n");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void visit(BooleanValue value) {
        try {
            if (value.getConstant())
                this.fileWriter.write("iconst_1\n");
            else
                this.fileWriter.write("iconst_0\n");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void visit(IntValue value) {
        try {
            this.fileWriter.write("iconst_"+value.getConstant()+"\n");
        } catch (Exception e) {
            System.out.println(e);
        }
//        System.out.println(value.getLine());
    }

    @Override
    public void visit(StringValue value) {
        try {
            this.fileWriter.write("ldc "+value.getConstant()+"\n");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void visit(Block block) {
        for(Statement st: block.getStatements()){
            st.accept(this);
        }
    }

    @Override
    public void visit(Conditional conditional) {
        try {
            int currentLabelNum = labelNum++;
            int nextLabelNum = labelNum++;
            conditional.getExpression().accept(this);
            this.fileWriter.write("ifeq L" + currentLabelNum + "\n");
            conditional.getThenBody().accept(this);
            this.fileWriter.write("goto L" + nextLabelNum + "\n");
            this.fileWriter.write("L" + currentLabelNum + ": \n");
            conditional.getElseBody().accept(this);
            this.fileWriter.write("L" + nextLabelNum + ": \n");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void visit(For loop) {
        try {
            int continueLabelNum = labelNum++;
            int breakLabelNum = labelNum++;
            Continue.push("continueLabel_"+continueLabelNum);
            Break.push("breakLabel_"+breakLabelNum);
            loop.getInitialize().accept(this);
            this.fileWriter.write("continueLabel_"+continueLabelNum+": \n");
            loop.getCondition().accept(this);
            this.fileWriter.write("ifeq "+"breakLabel_"+breakLabelNum+"\n");
            loop.getBody().accept(this);
            //printOut("HEREEEE");
            loop.getUpdate().accept(this);
            //printOut("NOWWWW");
            this.fileWriter.write("goto continueLabel_"+continueLabelNum+"\n");
            this.fileWriter.write("breakLabel_"+breakLabelNum+":\n");
            Continue.pop();
            Break.pop();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void visit(Break breakLoop) {
        try {
            this.fileWriter.write("goto "+Break.peek());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void visit(Continue continueLoop) {
        try {
            this.fileWriter.write("goto "+Continue.peek());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void visit(MsgHandlerCall msgHandlerCall) {
//        System.out.println(msgHandlerCall.getLine());
//        for (Expression e : msgHandlerCall.getArgs()) {
//            e.accept(this);
//        }
        try {
            if(msgHandlerCall.getInstance() instanceof Identifier) {
                this.fileWriter.write("aload_0\n");
                for (String s : knownActors) {
                    if (s.split("#", 4)[0].equals(actorName) &&
                            s.split("#", 4)[1].equals(((Identifier) msgHandlerCall.getInstance()).getName())) {
                        this.fileWriter.write("getfield " + actorName
                                + "/" + ((Identifier) msgHandlerCall.getInstance()).getName() + " " + returnType(s.split("#", 4)[2]) + "\n");
                    }
                }
                this.fileWriter.write("aload_0\n");
                for (Expression exp : msgHandlerCall.getArgs()) {
                    exp.accept(this);
                }
                String inArgs = "";
                for (String s : handlers) {
                    if( s.split("#", 3)[1].equals(msgHandlerCall.getMsgHandlerName().getName())) {
                        inArgs = s.split("#")[2];
                    }
                }
                this.fileWriter.write("invokevirtual "
                        + ((Identifier) msgHandlerCall.getInstance()).getType().toString() + "/send_"
                        + msgHandlerCall.getMsgHandlerName().getName() + "(LActor;"+ inArgs + ")V\n");
            }
//            System.out.println(msgHandlerCall.getLine());
            if(msgHandlerCall.getInstance() instanceof Sender) {
                this.fileWriter.write("aload_1\n");
                this.fileWriter.write("aload_0\n");
                for (Expression exp : msgHandlerCall.getArgs()) {
                    exp.accept(this);
                }
                String inArgs = "";
                for (String s : handlers) {
                    if( s.split("#", 3)[1].equals(msgHandlerCall.getMsgHandlerName().getName())) {
                        inArgs = s.split("#")[2];
                    }
                }
                this.fileWriter.write("invokevirtual " + "Actor" + "/send_"
                        + msgHandlerCall.getMsgHandlerName().getName() + "(LActor;"+ inArgs + ")V\n");
            }
            if(msgHandlerCall.getInstance() instanceof Self) {
                this.fileWriter.write("aload_0\n");
                this.fileWriter.write("aload_0\n");
                for (Expression exp : msgHandlerCall.getArgs()) {
                    exp.accept(this);
                }
                String inArgs = "";
                for (String s : handlers) {
                    if( s.split("#", 3)[1].equals(msgHandlerCall.getMsgHandlerName().getName())) {
                        inArgs = s.split("#")[2];
                    }
                }
                this.fileWriter.write("invokevirtual " + "Actor" + "/send_"
                        + msgHandlerCall.getMsgHandlerName().getName() + "(LActor;"+ inArgs + ")V\n");
            }

//            System.out.println(msgHandlerCall.getLine());
            printOut("***");
        } catch (Exception e) {
            System.out.println(e);
        }
//        System.out.println(msgHandlerCall.getLine());
    }

    @Override
    public void visit(Print print) {
        try {
            this.fileWriter.write("getstatic java/lang/System/out Ljava/io/PrintStream;\n");
            if(print.getArg().getType().toString().equals("int") ||  print.getArg().getType().toString() .equals("boolean")) {
                print.getArg().accept(this);
                this.fileWriter.write("invokevirtual java/io/PrintStream/println(I)V\n");
            }
            if(print.getArg().getType().toString().equals("string")){
                print.getArg().accept(this);
                this.fileWriter.write("invokevirtual  java/io/PrintStream/println(Ljava/lang/String;)V\n");
            }
            if(print.getArg().getType().toString().equals("int[]")){
                print.getArg().accept(this);
                this.fileWriter.write("invokestatic java/util/Arrays/toString([I)Ljava/lang/String;");
                this.fileWriter.write("invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V\n");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void visit(Assign assign) {
        try {
            boolean iisSeen = false;
            if(assign.getrValue() instanceof Identifier) {
                for (String s : msgArgs) {
                    if(s.split("#", 4)[0].equals(actorName)
                            && s.split("#", 4)[3].equals(((Identifier) assign.getrValue()).getName())) {
                        if(s.split("#", 4)[1].equals("int") || s.split("#", 4)[1].equals("boolean")) {
                            iisSeen = true;
                            this.fileWriter.write("iload_" + s.split("#", 4)[2]);
                        }
                        else if (s.split("#", 4)[1].equals("int[]")) {
                            this.fileWriter.write("iaload");
                        }
                        else {
                            this.fileWriter.write("aload_" + s.split("#", 4)[2]);
                        }
                    }
                }
                for (String s : msgLocals) {
                    if(s.split("#", 4)[0].equals(actorName)
                            && s.split("#", 4)[3].equals(((Identifier) assign.getrValue()).getName())) {
                        if(s.split("#", 4)[1].equals("int") || s.split("#", 4)[1].equals("boolean")) {
                            iisSeen = true;
                            this.fileWriter.write("iload_" + s.split("#", 4)[2]);
                        }
                        else if (s.split("#", 4)[1].equals("int[]")) {
                            this.fileWriter.write("iaload");
                        }
                        else {
                            this.fileWriter.write("aload_" + s.split("#", 4)[2]);
                        }
                    }
                }
            }
            if(!iisSeen) {
                assign.getrValue().accept(this);
            }
            seen = false;
            isLeftHandSide = true;
            iisSeen = false;
            if(assign.getrValue() instanceof Identifier) {
                for (String s : msgArgs) {
                    if(s.split("#", 4)[0].equals(actorName)
                            && s.split("#", 4)[3].equals(((Identifier) assign.getlValue()).getName())) {
                        if(s.split("#", 4)[1].equals("int") || s.split("#", 4)[1].equals("boolean")) {
                            iisSeen = true;
                            this.fileWriter.write("iload_" + s.split("#", 4)[2]);
                        }
                        else if (s.split("#", 4)[1].equals("int[]")) {
                            this.fileWriter.write("iaload");
                        }
                        else {
                            this.fileWriter.write("aload_" + s.split("#", 4)[2]);
                        }
                    }
                }
                for (String s : msgLocals) {
                    if(s.split("#", 4)[0].equals(actorName)
                            && s.split("#", 4)[3].equals(((Identifier) assign.getlValue()).getName())) {
                        if(s.split("#", 4)[1].equals("int") || s.split("#", 4)[1].equals("boolean")) {
                            iisSeen = true;
                            this.fileWriter.write("istore_" + s.split("#", 4)[2]);
                        }
                        else if (s.split("#", 4)[1].equals("int[]")) {
                            this.fileWriter.write("iastore");
                        }
                        else {
                            this.fileWriter.write("astore_" + s.split("#", 4)[2]);
                        }
                    }
                }
            }
            if(!iisSeen) {
                assign.getlValue().accept(this);
            }
            isLeftHandSide = false;
        }catch (Exception e) {
            System.out.println(e);
        }
    }

}
