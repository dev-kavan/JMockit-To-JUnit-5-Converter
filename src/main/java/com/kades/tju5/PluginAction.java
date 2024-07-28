package com.kades.tju5;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;


public class PluginAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        // Get the current file
        final PsiFile file = e.getData(CommonDataKeys.PSI_FILE);

        // Perform action if file contains content
        if (file != null && !file.getText().isEmpty()) {

            final String code = file.getText();

            String updatedCode = ConverterUtil.removeMockitImports(code);
            updatedCode = ConverterUtil.updateJUnitImports(updatedCode);
            updatedCode = ConverterUtil.updateClassAnnotations(updatedCode);
            updatedCode = ConverterUtil.updateMockStatements(updatedCode);
            updatedCode = ConverterUtil.updateVerificationStatements(updatedCode);

            // Get the document to perform write operation
            final Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);

            if (document != null) {

                // Overwrite the existing file with updated conversion to JUnit 5
                String finalUpdatedCode = updatedCode;
                WriteCommandAction.runWriteCommandAction(
                        file.getProject(),
                        () -> document.setText(finalUpdatedCode)
                );

                try {
                    wait(10000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }

                WriteCommandAction.runWriteCommandAction(
                        file.getProject(),
                        () -> document.setText(code)
                );
            }
        }
    }
}
