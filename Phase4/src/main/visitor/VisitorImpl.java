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

import main.ast.type.Type;
import main.ast.type.actorType.ActorType;
import main.ast.type.arrayType.ArrayType;
import main.ast.type.noType.NoType;
import main.ast.type.primitiveType.BooleanType;
import main.ast.type.primitiveType.IntType;
import main.ast.type.primitiveType.StringType;


public class VisitorImpl implements Visitor {
    private ArrayList<String> errorMessages = new ArrayList<String>();
    private ArrayList<ArrayList<String>> splitedErrors = new ArrayList<ArrayList<String>>();
    private ArrayList<String> actorNames = new ArrayList<String>();
    private ArrayList<String> knownActorNames = new ArrayList<String>();
    private ArrayList<String> actorVarNames = new ArrayList<String>();
    private ArrayList<String> msgHandlerNames = new ArrayList<String>();
    private ArrayList<String> msgHandlerNames2 = new ArrayList<String>();
    private ArrayList<String> msgHandlerArgsNames = new ArrayList<String>();
    private ArrayList<String> msgHandlerLocalVarsNames = new ArrayList<String>();
    private ArrayList<String> blocks = new ArrayList<String>();
    private ArrayList<String> varDeclarationNames = new ArrayList<String>();
    private ArrayList<String> msgHandlerArgsNames2 = new ArrayList<String>();
    private ArrayList<String> parents = new ArrayList<String>();
    private ArrayList<String> mainActorNames = new ArrayList<String>();
    private ArrayList<String> knownActorNamesMain = new ArrayList<String>();
    private ArrayList<String> arraySizeActor = new ArrayList<String>();
    private ArrayList<String> arraySizeHandler = new ArrayList<String>();
    private ArrayList<String> argMain = new ArrayList<String>();

    private int blockCounter = 0;
    private int globalThisBlock = 0;
    private String currentActor = "";
    private String currentHandler = "";
    private String currentType = "";
    private boolean seenBreak = false;

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

    private boolean isSubtype(Type right, Type left) {
        if(left.toString().equals(right.toString())){
            return true;
        }
        if(!(right.toString().equals("int") || right.toString().equals("boolean") || right.toString().equals("string")
                || right.toString().equals("int[]")) && !(left.toString().equals("int") || left.toString().equals("boolean")
                || left.toString().equals("string") || left.toString().equals("int[]"))) {
            return true;
        }
        return right.toString().equals("notype");
    }

    private Type returnType(String toString, Identifier id) {
        switch (toString) {
            case "int":
                return new IntType();
            case "boolean":
                return new BooleanType();
            case "int[]":
                return new ArrayType(1);
            case "string":
                return new StringType();
            default:
                return new NoType();
        }
    }

    private Type returnTypeActorVarAccess(String toString) {
        switch (toString) {
            case "int":
                return new IntType();
            case "boolean":
                return new BooleanType();
            case "int[]":
                return new ArrayType(1);
            case "string":
                return new StringType();
            default:
                return new NoType();
        }
    }

    private void firstOfAll(Program program) {
        ArrayList<String> knownActorNamesTemp = new ArrayList<String>();
        ArrayList<String> actorVarNamesTemp = new ArrayList<String>();
        ArrayList<String> msgHandlerNamesTemp = new ArrayList<String>();
        ArrayList<String> msgHandlerArgsNamesTemp = new ArrayList<String>();
        ArrayList<String> msgHandlerLocalVarsNamesTemp = new ArrayList<String>();
        ArrayList<String> arraySizeActorTemp = new ArrayList<String>();
        ArrayList<String> arraySizeHandlerTemp = new ArrayList<String>();

        for (ActorDeclaration actorDeclaration : program.getActors()) {
            actorNames.add(actorDeclaration.getName().getName());
        }

        for (ActorDeclaration actorDeclaration: program.getActors()) {
            currentActor = actorDeclaration.getName().getName();
            if(actorDeclaration.getParentName() != null) {
                parents.add(actorDeclaration.getName().getName() + "#" + actorDeclaration.getParentName().getName());
                if (!actorNames.contains(actorDeclaration.getParentName().getName()))
                    errorMessages.add(actorDeclaration.getParentName().getLine() + "#actor "
                            + actorDeclaration.getParentName().getName() + " is not declared");
            }
            else
                parents.add(actorDeclaration.getName().getName() + "#$noParent");
            for (VarDeclaration knownActor : actorDeclaration.getKnownActors()) {
                if(actorNames.contains(knownActor.getType().toString())) {
                    knownActorNames.add(actorDeclaration.getName().getName() + "#" +
                            knownActor.getIdentifier().getName() + "#" + knownActor.getType().toString());
                    knownActorNamesMain.add(actorDeclaration.getName().getName() + "#" +
                            knownActor.getIdentifier().getName() + "#" + knownActor.getType().toString());
                }
                else {
                    errorMessages.add(knownActor.getIdentifier().getLine() + "#actor "
                            + knownActor.getType().toString() + " is not declared");
                    knownActorNamesMain.add(actorDeclaration.getName().getName() + "#" +
                            knownActor.getIdentifier().getName() + "#" + knownActor.getType().toString());
                }
            }
            for (VarDeclaration actorVar : actorDeclaration.getActorVars()) {
                actorVarNames.add(actorDeclaration.getName().getName() + "#" +
                        actorVar.getIdentifier().getName() + "#" + actorVar.getType().toString());
                if(actorVar.getType() instanceof ArrayType) {
                    arraySizeActor.add(actorVar.getIdentifier().getName() + "#" + ((ArrayType) actorVar.getType()).getSize() + "#"
                            + currentActor);
                }
            }
            if(actorDeclaration.getInitHandler() != null) {
                HandlerDeclaration handlerDeclaration = actorDeclaration.getInitHandler();
                for (VarDeclaration arg : handlerDeclaration.getArgs()) {
                    msgHandlerArgsNames.add(handlerDeclaration.getName().getName()
                            + "#" + arg.getType().toString() + "#"  + currentActor);
                    msgHandlerArgsNames2.add(handlerDeclaration.getName().getName()
                            + "#" + arg.getIdentifier().getName() + "#"  + currentActor + "#" + arg.getType().toString());
                    argMain.add(handlerDeclaration.getName().getName()
                            + "#" + arg.getType().toString() + "#"  + currentActor);
                }
                for (VarDeclaration localVar : handlerDeclaration.getLocalVars()) {
                    msgHandlerLocalVarsNames.add(handlerDeclaration.getName().getName()
                            + "#" + localVar.getIdentifier().getName() + "#" + localVar.getType().toString() + "#" + currentActor);
                }
                msgHandlerNames.add(actorDeclaration.getName().getName() + "#" +
                        actorDeclaration.getInitHandler().getName().getName());
            }
            for (HandlerDeclaration handlerDeclaration : actorDeclaration.getMsgHandlers()) {
                currentHandler = handlerDeclaration.getName().getName();
                for (VarDeclaration arg : handlerDeclaration.getArgs()) {
                    msgHandlerArgsNames.add(handlerDeclaration.getName().getName()
                            + "#" + arg.getType().toString() + "#"  + currentActor);
                    msgHandlerArgsNames2.add(handlerDeclaration.getName().getName()
                            + "#" + arg.getIdentifier().getName() + "#"  + currentActor + "#" + arg.getType().toString());
                    argMain.add(handlerDeclaration.getName().getName()
                            + "#" + arg.getType().toString() + "#"  + currentActor);
                }
                for (VarDeclaration localVar : handlerDeclaration.getLocalVars()) {
                    msgHandlerLocalVarsNames.add(handlerDeclaration.getName().getName()
                            + "#" + localVar.getIdentifier().getName() + "#" + localVar.getType().toString() + "#" + currentActor);
                    if(localVar.getType() instanceof ArrayType) {
                        arraySizeHandler.add(localVar.getIdentifier().getName() + "#" + ((ArrayType) localVar.getType()).getSize()
                                + "#" + currentActor + "#" +currentHandler);
                    }
                }
                msgHandlerNames.add(actorDeclaration.getName().getName() + "#" +
                        handlerDeclaration.getName().getName());
            }
        }
        for (ActorDeclaration actorDeclaration: program.getActors()) {
            for (String str : parents) {
                if(str.split("#", 2)[0].equals(actorDeclaration.getName().getName())) {
                    String now = str;
                    String man = now.split("#", 2)[0];
                    while (true) {
                        if(!(actorNames.contains(now.split("#", 2)[1])))
                            break;
                        for (String str1 : knownActorNames) {
                            if(str1.split("#", 2)[0].equals(now.split("#", 2)[1])) {
                                knownActorNamesTemp.add(man +
                                        "#" + str1.split("#", 2)[1]);
                            }
                        }
                        knownActorNames.addAll(knownActorNamesTemp);
                        knownActorNamesTemp.clear();
                        for (String str1 : actorVarNames) {
                            if(str1.split("#", 2)[0].equals(now.split("#", 2)[1])) {
                                actorVarNamesTemp.add(man +
                                        "#" + str1.split("#", 2)[1]);
                            }
                        }
                        actorVarNames.addAll(actorVarNamesTemp);
                        actorVarNamesTemp.clear();
                        for (String str1 : msgHandlerNames) {
                            if(str1.split("#", 2)[0].equals(now.split("#", 2)[1])) {
                                msgHandlerNamesTemp.add(man +
                                        "#" + str1.split("#", 2)[1]);
                            }
                        }
                        msgHandlerNames.addAll(msgHandlerNamesTemp);
                        msgHandlerNamesTemp.clear();
                        for (String str1 : msgHandlerArgsNames) {
                            if(str1.split("#", 2)[0].equals(now.split("#", 2)[1])) {
                                msgHandlerArgsNamesTemp.add(man +
                                        "#" + str1.split("#", 2)[1]);
                            }
                        }
                        msgHandlerArgsNames.addAll(msgHandlerArgsNamesTemp);
                        msgHandlerArgsNamesTemp.clear();
                        for (String str1 : msgHandlerLocalVarsNames) {
                            if(str1.split("#", 2)[0].equals(now.split("#", 2)[1])) {
                                msgHandlerLocalVarsNamesTemp.add(man +
                                        "#" + str1.split("#", 2)[1]);
                            }
                        }
                        msgHandlerLocalVarsNames.addAll(msgHandlerLocalVarsNamesTemp);
                        msgHandlerLocalVarsNamesTemp.clear();
                        if(now.split("#", 2)[1].equals("$noParent"))
                            break;
                        for (String s : parents) {
                            if(s.split("#", 2)[0].equals(now.split("#", 2)[1])) {
                                now = s;
                                break;
                            }
                        }
                        for (String str1 : arraySizeActor) {
                            if(str1.split("#", 3)[2].equals(now.split("#", 2)[1])) {
                                arraySizeActorTemp.add((str1.split("#", 3)[0] + "#" + str1.split("#", 3)[1] + "#" + man));
                            }
                        }
                        arraySizeActor.addAll(arraySizeActorTemp);
                        arraySizeActorTemp.clear();
                        for (String str1 : arraySizeHandler) {
                            if(str1.split("#", 3)[2].equals(now.split("#", 2)[1])) {
                                arraySizeHandlerTemp.add((str1.split("#", 4)[0] + "#" +  str1.split("#", 4)[1] + "#" + man
                                        + "#" + str1.split("#", 4)[3]));
                            }
                        }
                        arraySizeHandler.addAll(arraySizeHandlerTemp);
                        arraySizeHandlerTemp.clear();
                    }
                }
            }
        }
    }

    private void checkInitial(ActorInstantiation ai) {
        currentActor = "$";
        for (Expression arg : ai.getInitArgs()) {
            arg.accept(this);
        }
        boolean noInput = true;
        for(int i = 0; i < argMain.size(); i++) {
            String findArg = argMain.get(i);
            if(findArg.split("#", 3)[2].equals(ai.getType().toString())
                    && findArg.split("#", 3)[0].equals("initial")) {
                noInput = false;
                boolean entered = false;
                int j;
                for (j = 0; j < ai.getInitArgs().size(); ++j) {
                    entered = true;
                    if(!argMain.get(i + j).split("#", 3)[2].equals(ai.getType().toString())) {
                        errorMessages.add(ai.getLine() + "#arguments do not match with definition");
                        return;
                    }
                    String whatType = argMain.get(i + j).split("#", 3)[1];
                    if(!whatType.equals(ai.getInitArgs().get(j).getType().toString())
                            && !ai.getInitArgs().get(j).getType().toString().equals("notype")) {
                        errorMessages.add(ai.getLine() + "#arguments do not match with definition");
                        return;
                    }
                }
                if(j < argMain.size()) {
                    if (argMain.get(i + j).split("#", 3)[0].equals(ai.getType().toString())) {
                        errorMessages.add(ai.getLine() + "#arguments do not match with definition");
                        return;
                    }
                }
                if(entered)
                    return;
            }
        }
        if(noInput && ai.getInitArgs().size() != 0) {
            errorMessages.add(ai.getLine() + "#arguments do not match with definition");
            return;
        }
        if(noInput && ai.getInitArgs().size() > 0) {
            errorMessages.add(ai.getLine() + "#arguments do not match with definition");
            return;
        }
        if(!noInput && ai.getInitArgs().size() == 0) {
            errorMessages.add(ai.getLine() + "#arguments do not match with definition");
            return;
        }
    }



    private void splitErrors(){
        for (String str : errorMessages) {
            String[] arrOfErrors = str.split("#",2);
            ArrayList<String> res = new ArrayList<String>();
            res.add(arrOfErrors[0]);
            res.add(arrOfErrors[1]);
            splitedErrors.add(res);
        }
    }

    private void sortErrors(){
        splitErrors();
        for(int i = 0; i < splitedErrors.size()-1; i++){
            for(int j = i+1; j < splitedErrors.size(); j++) {
                if (Integer.parseInt(splitedErrors.get(i).get(0)) > Integer.parseInt(splitedErrors.get(j).get(0))) {
                    Collections.swap(splitedErrors, i, j);
                }
            }
        }
    }

    private void printErrors(){
        sortErrors();
        for(int i = 0; i < splitedErrors.size(); i++){
            System.out.print("Line:");
            System.out.print(splitedErrors.get(i).get(0));
            System.out.print(":");
            System.out.println(splitedErrors.get(i).get(1));
        }
    }

    private String findSizeRight(Assign assign) {
        String rightSize = "";
        for (String rightString : arraySizeActor) {
            if(assign.getrValue() instanceof Identifier) {
                if (rightString.split("#", 3)[0].equals(((Identifier) assign.getrValue()).getName())
                        && rightString.split("#", 3)[2].equals(currentActor)) {
                    rightSize = rightString.split("#", 3)[1];
                    return rightSize;
                }

            }
        }
        for (String rightString : arraySizeHandler) {
            if(assign.getrValue() instanceof Identifier) {
                if (rightString.split("#", 4)[0].equals(((Identifier) assign.getrValue()).getName())
                        && rightString.split("#", 4)[2].equals(currentActor)
                        && rightString.split("#", 4)[3].equals(currentHandler)) {
                    rightSize = rightString.split("#", 4)[1];
                    return rightSize;
                }
            }
        }
        for (String rightString : arraySizeActor) {
            if(assign.getrValue() instanceof ActorVarAccess) {
                if (rightString.split("#", 3)[0].equals(((Identifier) assign.getrValue()).getName())
                        && rightString.split("#", 3)[2].equals(currentActor)) {
                    rightSize = rightString.split("#", 3)[1];
                    return rightSize;
                }

            }
        }
        for (String rightString : arraySizeHandler) {
            if(assign.getrValue() instanceof ActorVarAccess) {
                if (rightString.split("#", 4)[0].equals(((Identifier) assign.getrValue()).getName())
                        && rightString.split("#", 4)[2].equals(currentActor)
                        && rightString.split("#", 4)[3].equals(currentHandler)) {
                    rightSize = rightString.split("#", 4)[1];
                    return rightSize;
                }
            }
        }
        for (String rightString : arraySizeActor) {
            if(assign.getrValue() instanceof ArrayCall) {
                if (rightString.split("#", 3)[0].equals(((ActorVarAccess) assign.getrValue()).getVariable())
                        && rightString.split("#", 3)[2].equals(currentActor)) {
                    rightSize = rightString.split("#", 3)[1];
                    return rightSize;
                }

            }
        }
        for (String rightString : arraySizeHandler) {
            if(assign.getrValue() instanceof ArrayCall) {
                if (rightString.split("#", 4)[0].equals(((ActorVarAccess) assign.getrValue()).getVariable())
                        && rightString.split("#", 4)[2].equals(currentActor)
                        && rightString.split("#", 4)[3].equals(currentHandler)) {
                    rightSize = rightString.split("#", 4)[1];
                    return rightSize;
                }
            }
        }
        return rightSize;
    }

    private String findSizeLeft(Assign assign) {
        String leftSize = "";
        for (String leftString : arraySizeActor) {
            if(assign.getlValue() instanceof Identifier) {
                if (leftString.split("#", 3)[0].equals(((Identifier) assign.getlValue()).getName())
                        && leftString.split("#", 3)[2].equals(currentActor)) {
                    leftSize = leftString.split("#", 3)[1];
                    return leftSize;
                }

            }
        }
        for (String leftString : arraySizeHandler) {
            if(assign.getlValue() instanceof Identifier) {
                if (leftString.split("#", 4)[0].equals(((Identifier) assign.getlValue()).getName())
                        && leftString.split("#", 4)[2].equals(currentActor)
                        && leftString.split("#", 4)[3].equals(currentHandler)) {
                    leftSize = leftString.split("#", 4)[1];
                    return leftSize;
                }
            }
        }
        for (String leftString : arraySizeActor) {
            if(assign.getlValue() instanceof ActorVarAccess) {
                if (leftString.split("#", 3)[0].equals(((ActorVarAccess) assign.getlValue()).getVariable().getName())
                        && leftString.split("#", 3)[2].equals(currentActor)) {
                    leftSize = leftString.split("#", 3)[1];
                    return leftSize;
                }

            }
        }
        for (String leftString : arraySizeHandler) {
            if(assign.getlValue() instanceof ActorVarAccess) {
                if (leftString.split("#", 4)[0].equals(((ActorVarAccess) assign.getlValue()).getVariable().getName())
                        && leftString.split("#", 4)[2].equals(currentActor)
                        && leftString.split("#", 4)[3].equals(currentHandler)) {
                    leftSize = leftString.split("#", 4)[1];
                    return leftSize;
                }
            }
        }
        return leftSize;
    }

    @Override
    public void visit(Program program) {
        firstOfAll(program);
        for (ActorDeclaration actor: program.getActors()) {
            currentActor = actor.getName().getName();
            actor.accept(this);
            actorNames.add(actor.getName().getName());
        }
        program.getMain().accept(this);
        //printErrors();
    }

    @Override
    public void visit(ActorDeclaration actorDeclaration) {
        if(actorDeclaration.getInitHandler() != null) {
            currentHandler = actorDeclaration.getInitHandler().getName().getName();
            actorDeclaration.getInitHandler().accept(this);
            currentHandler = "";
        }
        for (HandlerDeclaration msgHandler : actorDeclaration.getMsgHandlers()) {
            currentHandler = msgHandler.getName().getName();
            msgHandler.accept(this);
            currentHandler = "";
        }
    }

    @Override
    public void visit(HandlerDeclaration handlerDeclaration) {
        for (Statement body : handlerDeclaration.getBody()) {
            body.accept(this);
        }
    }

    @Override
    public void visit(VarDeclaration varDeclaration) {
        varDeclarationNames.add(varDeclaration.getIdentifier().getName() + "#"
                + varDeclaration.getType().toString() + "#" + globalThisBlock);
    }


    @Override
    public void visit(UnaryExpression unaryExpression) {
        unaryExpression.getOperand().accept(this);
        if(unaryExpression.getUnaryOperator() == UnaryOperator.minus || unaryExpression.getUnaryOperator() == UnaryOperator.postdec
                || unaryExpression.getUnaryOperator() == UnaryOperator.postinc || unaryExpression.getUnaryOperator() == UnaryOperator.predec
                || unaryExpression.getUnaryOperator() == UnaryOperator.preinc) {
            if(!(unaryExpression.getOperand().getType().toString().equals("int") || unaryExpression.getOperand().getType().toString().equals("notype"))) {
                errorMessages.add(unaryExpression.getLine() + "#unsupported operand type for "
                        + unaryExpression.getUnaryOperator().toString());
                unaryExpression.setType(new NoType());
            }
            else if(unaryExpression.getOperand().getType().toString().equals("int")){
                unaryExpression.setType(new IntType());
            }
            else{
                unaryExpression.setType(new NoType());
            }
        }
        if(unaryExpression.getUnaryOperator() == UnaryOperator.not) {
            if(!(unaryExpression.getOperand().getType().toString().equals("boolean") || unaryExpression.getOperand().getType().toString().equals("notype"))) {
                errorMessages.add(unaryExpression.getLine() + "#unsupported operand type for "
                        + unaryExpression.getUnaryOperator().toString());
                unaryExpression.setType(new NoType());
            }
            else if(unaryExpression.getOperand().getType().toString().equals("boolean")){
                unaryExpression.setType(new BooleanType());
            }
            else{
                unaryExpression.setType(new NoType());
            }
        }
        if(unaryExpression.getUnaryOperator() == UnaryOperator.postdec ||unaryExpression.getUnaryOperator() == UnaryOperator.predec) {
            if(!(unaryExpression.getOperand() instanceof Identifier || unaryExpression.getOperand() instanceof ArrayCall
                    || unaryExpression.getOperand() instanceof ActorVarAccess)) {
                errorMessages.add(unaryExpression.getLine() + "#lvalue required as decrement operand");
            }
        }
        if(unaryExpression.getUnaryOperator() == UnaryOperator.postinc || unaryExpression.getUnaryOperator() == UnaryOperator.preinc) {
            if (!(unaryExpression.getOperand() instanceof Identifier || unaryExpression.getOperand() instanceof ArrayCall
                    || unaryExpression.getOperand() instanceof ActorVarAccess)) {
                errorMessages.add(unaryExpression.getLine() + "#lvalue required as increment operand");
            }
        }
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {
        binaryExpression.getLeft().accept(this);
        binaryExpression.getRight().accept(this);
        if(binaryExpression.getBinaryOperator() == BinaryOperator.eq || binaryExpression.getBinaryOperator() == BinaryOperator.neq) {
            if(!(isSubtype(binaryExpression.getRight().getType(), binaryExpression.getLeft().getType())
                    || isSubtype(binaryExpression.getLeft().getType(), binaryExpression.getRight().getType()))) {
                errorMessages.add(binaryExpression.getLine() + "#unsupported operand type for " + binaryExpression.getBinaryOperator().toString());
                binaryExpression.setType(new NoType());
            }
            else{
                binaryExpression.setType(new BooleanType());
            }
        }
        if(binaryExpression.getBinaryOperator() == BinaryOperator.assign) {
            if(!(isSubtype(binaryExpression.getRight().getType(), binaryExpression.getLeft().getType())
                    || isSubtype(binaryExpression.getLeft().getType(), binaryExpression.getRight().getType()))) {
                errorMessages.add(binaryExpression.getLine() + "#unsupported operand type for " + BinaryOperator.assign.toString());
                binaryExpression.setType(new NoType());
            }
            else {
                binaryExpression.setType(binaryExpression.getLeft().getType());
            }
        }
        if(binaryExpression.getBinaryOperator() == BinaryOperator.and || binaryExpression.getBinaryOperator() == BinaryOperator.or) {
            if(!(isSubtype(binaryExpression.getRight().getType(), new BooleanType())
                    && isSubtype(binaryExpression.getLeft().getType(), new BooleanType()))) {
                errorMessages.add(binaryExpression.getLine() + "#unsupported operand type for "
                        + binaryExpression.getBinaryOperator().toString());
                binaryExpression.setType(new NoType());
            }else if(binaryExpression.getRight().getType().toString().equals("notype") || binaryExpression.getLeft().getType().toString().equals("notype")) {
                binaryExpression.setType(new NoType());
            } else {
                binaryExpression.setType(new BooleanType());
            }
        }
        if(binaryExpression.getBinaryOperator() == BinaryOperator.mult || binaryExpression.getBinaryOperator() == BinaryOperator.div
                || binaryExpression.getBinaryOperator() == BinaryOperator.add || binaryExpression.getBinaryOperator() == BinaryOperator.sub
                || binaryExpression.getBinaryOperator() == BinaryOperator.mod) {
            if (!(isSubtype(binaryExpression.getRight().getType(), new IntType())
                    && isSubtype(binaryExpression.getLeft().getType(), new IntType()))) {
                errorMessages.add(binaryExpression.getLine() + "#unsupported operand type for "
                        + binaryExpression.getBinaryOperator().toString());
                binaryExpression.setType(new NoType());
            } else if(binaryExpression.getRight().getType().toString().equals("notype") || binaryExpression.getLeft().getType().toString().equals("notype")) {
                binaryExpression.setType(new NoType());
            } else {
                binaryExpression.setType(new IntType());
            }
        }
        if(binaryExpression.getBinaryOperator() == BinaryOperator.gt
                || binaryExpression.getBinaryOperator() == BinaryOperator.lt) {
            if (!(isSubtype(binaryExpression.getRight().getType(), new IntType())
                    && isSubtype(binaryExpression.getLeft().getType(), new IntType()))) {
                errorMessages.add(binaryExpression.getLine() + "#unsupported operand type for "
                        + binaryExpression.getBinaryOperator().toString());
                binaryExpression.setType(new NoType());
            } else if(binaryExpression.getRight().getType().toString().equals("notype") || binaryExpression.getLeft().getType().toString().equals("notype")) {
                binaryExpression.setType(new NoType());
            } else {
                binaryExpression.setType(new BooleanType());
            }
        }
    }

    @Override
    public void visit(Identifier identifier) {
        boolean foundIt = false;
        for (String str : knownActorNames) {
            if(str.split("#", 3)[1].equals(identifier.getName()) && str.split("#", 3)[0].equals(currentActor)){
                identifier.setType(new ActorType(new Identifier(str.split("#", 3)[2])));
                foundIt = true;
            }
        }
        for (String str : actorVarNames) {
            if(str.split("#", 3)[1].equals(identifier.getName()) && str.split("#", 3)[0].equals(currentActor)){
                identifier.setType(returnType(str.split("#", 3)[2], identifier));
                foundIt = true;
            }
        }
        for (String str : msgHandlerArgsNames2) {
            if (str.split("#", 4)[1].equals(identifier.getName()) && str.split("#", 4)[0].equals(currentHandler)
                    &&str.split("#", 4)[2].equals(currentActor)) {
                identifier.setType(returnType(str.split("#", 4)[3], identifier));
                foundIt = true;
            }
        }
        for (String str : msgHandlerLocalVarsNames) {
            if (str.split("#", 4)[1].equals(identifier.getName()) && str.split("#", 4)[0].equals(currentHandler)
                    &&str.split("#", 4)[3].equals(currentActor)) {
                identifier.setType(returnType(str.split("#", 4)[2], identifier));
                foundIt = true;
            }
        }
        if(!foundIt) {
            errorMessages.add(identifier.getLine() + "#variable " + identifier.getName() +" is not declared");
            identifier.setType(new NoType());
        }
    }

    @Override
    public void visit(Block block) {
        blockCounter ++;
        int thisBlock = blockCounter;
        globalThisBlock = thisBlock;
        blocks.add("Block#" + blockCounter);
        for (Statement S : block.getStatements()) {
            S.accept(this);
        }
        blocks.add("End#" + thisBlock);
        globalThisBlock --;
    }

    @Override
    public void visit(Conditional conditional) {
        if(conditional.getExpression() != null) {
            conditional.getExpression().accept(this);
            if (!(conditional.getExpression().getType().toString().equals("boolean")
                    || conditional.getExpression().getType().toString().equals("notype"))) {
                errorMessages.add(conditional.getExpression().getLine() + "#" + "condition type must be Boolean");
            }
        }
        if(conditional.getThenBody() != null) {
            conditional.getThenBody().accept(this);
        }
        if(conditional.getElseBody() != null) {
            conditional.getElseBody().accept(this);
        }
    }

    @Override
    public void visit(For loop) {
        if(loop.getInitialize() != null) {
            loop.getInitialize().getlValue().accept(this);
            loop.getInitialize().getlValue().accept(this);
        }
        if(loop.getUpdate() != null) {
            loop.getUpdate().getlValue().accept(this);
            loop.getUpdate().getlValue().accept(this);
        }
        if(loop.getCondition() != null) {
            loop.getCondition().accept(this);
            if (!(loop.getCondition().getType().toString().equals("boolean")
                    || loop.getCondition().getType().toString().equals("notype"))) {
                errorMessages.add(loop.getCondition().getLine() + "#condition type must be Boolean");
            }
        }
        if(loop.getBody() != null) {
            seenBreak = true;
            loop.getBody().accept(this);
            seenBreak = false;
        }
    }

    @Override
    public void visit(MsgHandlerCall msgHandlerCall) {
        if(msgHandlerCall.getInstance() instanceof Self) {
            msgHandlerCall.getInstance().accept(this);
            boolean foundIt = false;
            for (String str : msgHandlerNames) {
                if(str.split("#", 2)[0].equals(currentActor)
                        && str.split("#", 2)[1].equals(msgHandlerCall.getMsgHandlerName().getName())) {
                    foundIt = true;
                }
            }
            if(!foundIt) {
                errorMessages.add(msgHandlerCall.getMsgHandlerName().getLine() + "#"
                        + "there is no msghandler name " + msgHandlerCall.getMsgHandlerName().getName()
                        + " in actor " + currentActor);
                return;
            }
        }
        else if(msgHandlerCall.getInstance()  instanceof Sender) {
            for (Expression arg : msgHandlerCall.getArgs()) {
                arg.accept(this);
            }
            if (currentHandler.equals("initial")) {
                errorMessages.add(msgHandlerCall.getMsgHandlerName().getLine() + "#" + "no sender in initial msghandler");
            }
        }
        else if(msgHandlerCall.getInstance() instanceof Identifier){
            msgHandlerCall.getInstance().accept(this);
            if(!(actorNames.contains(((Identifier) msgHandlerCall.getInstance()).getName()))) {
                boolean flag = false;
                for (String str : knownActorNames) {
                    if(
                            str.split("#", 3)[1].equals(((Identifier) msgHandlerCall.getInstance()).getName())
                                    && str.split("#", 3)[0].equals(currentActor)) {
                        flag = true;
                    }
                }
                if(!flag) {
                    errorMessages.add(msgHandlerCall.getInstance().getLine() + "#variable "
                            + ((Identifier) msgHandlerCall.getInstance()).getName() + " is not callable");
                    return;
                }
            }
            if(!(msgHandlerNames.contains(((Identifier) msgHandlerCall.getInstance()).getType().toString() + "#" +
                    msgHandlerCall.getMsgHandlerName().getName()))) {
                errorMessages.add(msgHandlerCall.getMsgHandlerName().getLine()
                        + "#there is no msghandler name " + msgHandlerCall.getMsgHandlerName().getName()
                        + " in actor " + ((Identifier) msgHandlerCall.getInstance()).getName());
                return;
            }
        }
    }

    @Override
    public void visit(Print print) {
        print.getArg().accept(this);
        if(!(print.getArg().getType().toString() == "int" || print.getArg().getType().toString() == "string"
                || print.getArg().getType().toString() == "int[]" || print.getArg().getType().toString() == "boolean"
                || print.getArg().getType().toString() == "notype")) {
            errorMessages.add(print.getLine() + "#unsupported type for print");
        }
        for (String str : knownActorNames) {
            if (print.getArg().getType().toString().equals(str.split("#", 3)[1])) {
                errorMessages.add(print.getLine() + "#unsupported type for print");
            }
        }
    }

    @Override
    public void visit(Assign assign) {
        assign.getlValue().accept(this);
        assign.getrValue().accept(this);
        if(!(isSubtype(assign.getrValue().getType(), assign.getlValue().getType())
                || isSubtype(assign.getlValue().getType(), assign.getrValue().getType()))) {
            errorMessages.add(assign.getLine() + "#unsupported operand type for " + BinaryOperator.assign);
            assign.getlValue().setType(new NoType());
        }
        if(!(assign.getlValue() instanceof ArrayCall || assign.getlValue() instanceof Identifier
                || assign.getlValue() instanceof ActorVarAccess)) {
            errorMessages.add(assign.getLine() + "#left side of assignment must be a valid lvalue");
        }
        if(assign.getrValue().getType() instanceof ArrayType && assign.getlValue().getType() instanceof ArrayType) {
            String rightSize = findSizeRight(assign);
            String leftSize = findSizeLeft(assign);
            if(!leftSize.equals(rightSize)) {
                errorMessages.add(assign.getLine() + "#" + "operation assign requires equal array sizes");
            }
        }
    }

    @Override
    public void visit(ActorVarAccess actorVarAccess) {
        if(currentActor.equals("$")) {
            errorMessages.add(actorVarAccess.getLine() + "#" + "self doesn't refer to any actor");
            actorVarAccess.setType(new NoType());
            return;
        }
        boolean foundIt = false;
        for (String str : actorVarNames) {
            if(str.split("#", 3)[0].equals(currentActor)
                    && str.split("#", 3)[1].equals(actorVarAccess.getVariable().getName())) {
                foundIt = true;
                actorVarAccess.setType(returnTypeActorVarAccess(str.split("#", 3)[2]));
            }
        }
        if(!foundIt) {
            errorMessages.add(actorVarAccess.getLine() + "#variable " + actorVarAccess.getVariable().getName() + " is not declared");
            actorVarAccess.setType(new NoType());
        }
    }

    @Override
    public void visit(Main mainActors) {
        for (ActorInstantiation ai :mainActors.getMainActors()) {
            if(!actorNames.contains(ai.getType().toString())) {
                errorMessages.add(ai.getLine() + "#actor " + ai.getType().toString() + " is not declared");
            }
            else
                mainActorNames.add(ai.getIdentifier().getName() + "#" + ai.getType().toString());
        }
        for (ActorInstantiation ai : mainActors.getMainActors()) {
            ai.accept(this);
        }
    }

    @Override
    public void visit(ActorInstantiation actorInstantiation) {
        for (Identifier id : actorInstantiation.getKnownActors()) {
            boolean foundIt = false;
            for (String str : mainActorNames) {
                if(str.split("#", 2)[0].equals(id.getName())) {
                    foundIt = true;
                    id.setType(new ActorType(new Identifier(str.split("#", 2)[1])));
                }
            }
            if(!foundIt) {
                errorMessages.add(id.getLine() + "#variable " + id.getName() + " is not declared");
                id.setType(new NoType());
            }
        }
        checkInitial(actorInstantiation);
    }

    @Override
    public void visit(BooleanValue value) {
        value.setType(new BooleanType());
    }

    @Override
    public void visit(IntValue value) {
        value.setType(new IntType());
    }

    @Override
    public void visit(StringValue value) {
        value.setType(new StringType());
    }

    @Override
    public void visit(ArrayCall arrayCall) {
        arrayCall.setType(new IntType());
        arrayCall.getArrayInstance().accept(this);
    }

    @Override
    public void visit(Self self) {
        self.setType(new ActorType(new Identifier(currentActor)));
        return;
    }

    @Override
    public void visit(Sender sender) {
        if(currentHandler.equals("initial")) {
            errorMessages.add(sender.getLine() + "#" + "no sender in initial msghandler");
        }
        sender.setType(new ActorType(new Identifier("")));
        return;
    }

    @Override
    public void visit(Break breakLoop) {
        if(!seenBreak) {
            errorMessages.add(breakLoop.getLine() + "#break statement not within loop");
        }
        return;
    }

    @Override
    public void visit(Continue continueLoop) {
        if(!seenBreak) {
            errorMessages.add(continueLoop.getLine() + "#continue statement not within loop");
        }
        return;
    }
}
