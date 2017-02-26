package org.sins.plugins;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * Created by sin on 2017/2/25.
 */
public class SetterAndGetterAction extends AnAction {

    private PsiClass cls;
    private Project project;
    private Editor editor;
    private PsiFile psiFile;

    @Override
    public void actionPerformed(AnActionEvent e) {
        //触发插件后会调用这个方法，用来执行具体的业务逻辑，

        //获取当前正在操作的编辑器
        editor = e.getData(PlatformDataKeys.EDITOR);
        //获取当前正在操作的工程
        project = e.getData(PlatformDataKeys.PROJECT);
        //获取当前正在操作的文件对象
        psiFile = e.getData(PlatformDataKeys.PSI_FILE);
        //xxx....不造是什么意思, 估摸着是获取当前的焦点吧 :-D
        CaretModel caretModel = editor.getCaretModel();
        //获取光标所在的文件中的字符位置
        int offset = caretModel.getOffset();
        //根据字符的偏移量,获取到...元素
        PsiElement element = psiFile.findElementAt(offset);
        //通过工具类,传入正在操作的元素, 获取指定字节码的对象
        cls = PsiTreeUtil.getParentOfType(element, PsiClass.class);


        new WriteCommandAction.Simple(project,psiFile) {

            @Override
            protected void run() throws Throwable {
                new CodeGenerator(cls).execute();
                System.out.println(" --- "+Thread.currentThread().getName());
            }
        }.execute();
    }

    //会在actionPerformed方法前执行，可以用来对是否显示插件的入口做判断
    @Override
    public void update(AnActionEvent e) {

    }
}
