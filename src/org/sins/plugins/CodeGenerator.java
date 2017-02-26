package org.sins.plugins;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.http.util.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sin on 2017/2/25.
 */
public class CodeGenerator {

    private final PsiClass cls;


    private List<PsiField> fields;
    private List<PsiMethod> methods;

    private final int GETTER = -1;
    private final int SETTER = -2;

    public CodeGenerator(PsiClass cls) {

        this.cls = cls;
        fields = new ArrayList<>();
        methods = new ArrayList<>();

        initFileds();
        initMethods();
    }

    private void initMethods() {
        PsiMethod[] allMethods = cls.getAllMethods();
        List<PsiMethod> psiMethods = Arrays.asList(allMethods);
        methods.addAll(psiMethods);
    }

    private void initFileds() {
        //获取所有的字段
        PsiField[] allFields = cls.getAllFields();
        List<PsiField> psiFields = Arrays.asList(allFields);
        fields.addAll(psiFields);
    }

    //执行写入代码的逻辑
    public void execute() {
        //遍历fileds
        for (PsiField field : fields) {
            //判断该字段是否被以下关键字修饰,如果是则不生成setter getter方法
            if (field.hasModifierProperty(PsiModifier.FINAL)    //final 常量
                    || field.hasModifierProperty(PsiModifier.TRANSIENT) //transient
                    || field.hasModifierProperty(PsiModifier.NATIVE))   //native,
                continue;

            //获取成员变量的名字
            String name = field.getName();
            //根据成员变量名生成getter&setter方法名
            String getterName = createMethodNameByField(name, GETTER);
            String setterName = createMethodNameByField(name, SETTER);
            //获取成员字段的类型
            String fieldType = field.getType().getCanonicalText();
            //判断field的getter方法是否存在
            if (!isMethodExit(getterName)) {    //如果不存在getter方法,则生成getter方法
                String getter = createGetterMethodByName(name, fieldType);
                insertMethod(getter);
            }
            //判断field的setter方法是否存在
            if (!isMethodExit(setterName)) {    //如果不存在则生成setter方法
                String setter = createSetterMethodByName(name, fieldType);
                insertMethod(setter);
            }
        }

    }

    private void insertMethod(String methodStr) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(cls.getProject());
        PsiMethod createMethod = factory.createMethodFromText(methodStr, cls);
        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(cls.getProject());
        styleManager.shortenClassReferences(cls.addBefore(createMethod, cls.getLastChild()));
    }

    private String createSetterMethodByName(String name, String fieldType) {
        String setterMethod =
                "public void methodName(Type paramsNam){ this.paramsNam = paramsNam;}";

        return setterMethod.replace("Type", fieldType)
                .replace("methodName", createMethodNameByField(name, SETTER))
                .replace("paramsNam", name);
    }

    private String createGetterMethodByName(String name, String type) {
        String getterMethod =
                "public Type methodName(){return this.paramsNam;}";

        return getterMethod.replace("Type", type)
                .replace("methodName", createMethodNameByField(name, GETTER))
                .replace("paramsNam", name);
    }

    private boolean isMethodExit(String methodName) {
        if (TextUtils.isEmpty(methodName)) {
            throw new NullPointerException("methodName can not be null");
        }

        final int size = methods.size();
        for (int i = 0; i < size; i++) {

            PsiMethod psiMethod = methods.get(i);
            String name = psiMethod.getName();
            if (methodName.equals(name)) {
                return true;
            }
        }

        return false;
    }

    private String createMethodNameByField(String filedName, int methodType) {
        if (TextUtils.isEmpty(filedName)) {
            throw new NullPointerException("filedName can not be null");
        }

        StringBuilder methodName = new StringBuilder();
        methodName.append(methodType == GETTER ? "get" : "set");
        char c = filedName.charAt(0);

        if (c >= 'a' && c <= 'z') {
            methodName.append(filedName.substring(0, 1).toUpperCase());
            if (filedName.length() > 1) {
                methodName.append(filedName.substring(1));
            }
        } else {
            methodName.append(filedName);
        }

        return methodName.toString();
    }


}
