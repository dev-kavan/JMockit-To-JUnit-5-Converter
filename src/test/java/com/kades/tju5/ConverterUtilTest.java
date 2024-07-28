package com.kades.tju5;

import net.sf.cglib.core.Converter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ConverterUtilTest {

    @Test
    void testRemoveMockitImports() {

        final String content = """
            import mockit.Expectations;
            import mockit.Verifications;
            import mockit.Tested;
            import mockit.Mocked;
        """;

        final String updatedContent = ConverterUtil.removeMockitImports(content);

        assertFalse(updatedContent.contains("mockit"));
    }

    @Test
    void testUpdateJUnitImports() {

        final String content = """
            import org.junit.Test;
            import org.junit.Before;
            import org.junit.Assert.assertTrue;
            import org.junit.Assert.assertFalse;
            import org.junit.Assert.assertEquals;
            import org.junit.Assert;
        """;

        final String updatedContent = ConverterUtil.updateJUnitImports(content);

        assertFalse(updatedContent.contains("junit.Assert"));
        assertFalse(updatedContent.contains("junit.Before"));
    }

    @Test
    void testUpdateMockStatements() {

        final String content = """
            new Expectations() {
                someMethod.get(id);
                result = 1L;
                someDao.get(id);
                result = new Object();
                someMethod.findUser(null);
                result = new NullPointerException();
            };
        """;

        final String updatedContent = ConverterUtil.updateMockStatements(content);

        assertFalse(updatedContent.contains("result"));
        assertFalse(updatedContent.contains("new NullPointerException()"));
    }

    @Test
    void testUpdateVerifyStatements() {

        final String content = """
                new Verifications() {
                    class.method("param1", "param2");
                    times = 1;
                    class2.method(param2);
                    times = 0;
                };
        """;

        final String updatedContent = ConverterUtil.updateVerificationStatements(content);

        assertFalse(updatedContent.contains("new Verifications() {"));
        assertFalse(updatedContent.contains("times = "));
        assertFalse(updatedContent.contains("}"));

        assertTrue(updatedContent.contains("verify(class, times(1)).method(\"param1\", \"param2\");\n\t\t"));
        assertTrue(updatedContent.contains("verify(class2, never()).method(param2);"));
    }
}
