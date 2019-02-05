package com.pictionary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Reflection {

    private File f = new File("reflection.html");
    private BufferedWriter bw;
    private ArrayList<Class> klase = new ArrayList<>();

    public void doReflection() {
        try {
            populateClassList();
            bw = new BufferedWriter(new FileWriter(f));

            bw.write("<html>");
            bw.write("<body>");
            bw.write("<h1>Reflection API</h1>"); 

            for (Class cl : klase) {
                bw.write("<h2>Class name</h2>");
                bw.write(cl.getName());

                displayModifiers(cl);
                displaySuperClasses(cl);
                displayFields(cl);
                displayConstructors(cl);
                displayMethods(cl);
                displayInterfaces(cl);

                bw.write("</body>");
                bw.write("</html>");
            }

        } catch (IOException ex) {
            Logger.getLogger(Reflection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                Logger.getLogger(Reflection.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    private void displayInterfaces(Object obj) throws IOException {
        bw.write("<h2>Interfaces</h2>");
        int count = 0;
        Class cl = obj.getClass();
        Class[] interfaceList = cl.getInterfaces();
        while (count < interfaceList.length) {
            String iName = interfaceList[count].getName();
            bw.write(iName);
            count++;
        }
    }

    private void displayMethods(Object obj) throws IOException {
        bw.write("<h2>Methods</h2>");
        int count = 0;
        Class cl = obj.getClass();
        Method[] stringMethods = cl.getMethods();
        while (count < stringMethods.length) {
            String methodName = stringMethods[count].getName();
            String returnType = stringMethods[count].
                    getReturnType().getName();
            bw.write(returnType + " " + methodName + "(");
            Class[] parameterTypes
                    = stringMethods[count].getParameterTypes();
            for (int paramcount = 0; paramcount
                    < parameterTypes.length; paramcount++) {
                String parameterName = parameterTypes[paramcount].getName();
                bw.write(" " + parameterName);
            }
            bw.write(");");
            count++;
        }
    }

    private void displayConstructors(Class cl) throws IOException {
        bw.write("<h2>Constructors</h2>");
        int count = 0;
        String constructorName = cl.getName();
        Constructor[] socketConstructors = cl.getConstructors();
        while (count < socketConstructors.length) {
            bw.write(constructorName + "( ");
            Class[] ConstructorParameterTypes
                    = socketConstructors[count].
                            getParameterTypes();
            for (int index = 0; index
                    < ConstructorParameterTypes.length;
                    index++) {
                String parameterString
                        = ConstructorParameterTypes[index].getName();
                bw.write(parameterString + " ");
            }
            bw.write(");");
            count++;
        }
    }

    private void displayFields(Class cl) throws IOException {
        bw.write("<h2>Fields</h2>");
        int count = 0;
        Field[] publicFields = cl.getFields();

        for (Field f : publicFields) {
            String fName = f.getName();
            Class fType = f.getType();
            String typeName = fType.getName();
            bw.write("Type: " + typeName + " Name: " + fName);
        }
    }

    private void displaySuperClasses(Object obj) throws IOException {
        bw.write("<h2>Superclasses</h2>");
        Class cl = obj.getClass();
        Class superclass = cl.getSuperclass();

        while (superclass != null) {
            String name = superclass.getName();
            bw.write("Superclass name: " + name);
            cl = superclass;
            superclass = cl.getSuperclass();
        }
    }

    private void displayModifiers(Class cl) throws IOException {
        bw.write("<h2>Class modifiers</h2>");
        int mod = cl.getModifiers();

        if (Modifier.isPublic(mod)) {
            bw.write("public");
        }
        if (Modifier.isPrivate(mod)) {
            bw.write("private");
        }
        if (Modifier.isAbstract(mod)) {
            bw.write("abstract");
        }
        if (Modifier.isFinal(mod)) {
            bw.write("final");
        }
    }

    private void populateClassList() {
        Class c1 = Pictionary.class;
            Class c2 = Reflection.class;
            Class c3 = com.pictionary.client.Client.class;
            Class c5 = com.pictionary.gui.GameScreenController.class;
            Class c6 = com.pictionary.gui.LoadScreenController.class;
            Class c7 = com.pictionary.gui.MakeDraggable.class;
            Class c8 = com.pictionary.model.Player.class;
            Class c9 = com.pictionary.server.Server.class;
            Class c10 = com.pictionary.server.ServerMain.class;
            Class c11 = com.pictionary.server.ServerWorker.class;

            klase.add(c1);
            klase.add(c2);
            klase.add(c3);
            klase.add(c5);
            klase.add(c6);
            klase.add(c7);
            klase.add(c8);
            klase.add(c9);
            klase.add(c10);
            klase.add(c11);
    }
}
