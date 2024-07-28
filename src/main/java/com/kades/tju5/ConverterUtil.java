package com.kades.tju5;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConverterUtil {

    public static String removeMockitImports(String content) {

        String updatedContent = content;

        final String regex = "\\bimport mockit\\..*";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            updatedContent = updatedContent.replaceAll(Pattern.quote(matcher.group()), Matcher.quoteReplacement(""));
        }

        return updatedContent.trim();
    }

    public static String updateJUnitImports(String content) {

        String updatedContent = content;

        final String regex = "\\bimport org\\.junit\\.([A-Za-z]*)\\;?\\.?([A-Za-z]*)?\\;?";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(content);

        final String prefix = "import org.junit.jupiter.api.";
        String postfix;

        while (matcher.find()) {

            final String dependencyClass = matcher.group(1);
            final String dependencyMethod = matcher.group(2);

            postfix = switch (dependencyClass) {
                case "Assert" -> dependencyMethod.isEmpty() ? null : "Assertions." + dependencyMethod + ";";
                case "Before" -> "BeforeEach;\n";
                case "After" -> "AfterEach;\n";
                default -> "Test;\n";
            };

            final String updatedImport = postfix != null ? prefix + postfix : "";

            updatedContent = updatedContent.replaceAll(
                    Pattern.quote(matcher.group()),
                    Matcher.quoteReplacement(updatedImport)
            );
        }

        return updatedContent.trim();
    }

    public static String updateClassAnnotations(String content) {

        String updatedContent = content;

        final String regex = "[^\\(]\\@([A-Za-z]*)";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {

            final String updatedAnnotation = switch (matcher.group(1)) {
                case "Before" -> " @BeforeEach";
                case "After" -> " @AfterEach";
                case "Injectable", "Mocked" -> " @Mock";
                case "Tested" -> " @InjectMocks";
                default -> " @Test";
            };

            updatedContent = updatedContent.replaceAll(
                    Pattern.quote(matcher.group()),
                    Matcher.quoteReplacement(updatedAnnotation)
            );
        }

        return updatedContent.trim();
    }

    public static String updateMockStatements(String content) {

        String updatedContent = content;

        final String regex = "\\bnew Expectations\\(\\)\\s?\\{\\{?([^}]*)\\}\\}?\\;?";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {

            final String mockStatements = updateToWhenThenStatements(matcher.group(1));

            updatedContent = updatedContent.replaceAll(
                    Pattern.quote(matcher.group()),
                    Matcher.quoteReplacement(mockStatements)
            );
        }

        return updatedContent.trim();
    }

    private static String updateToWhenThenStatements(String content) {

        String updatedContent = content;

        final String regex = "\\s*(.*);\\s*result\\s?=\\s?(.*);";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {

            final String methodCall = matcher.group(1);
            String result = matcher.group(2);

            if (result.contains("new") && result.contains("Exception")) {

                final String resultRegex = "new ([A-Za-z]*)";
                final Pattern resultPattern = Pattern.compile(resultRegex);
                final Matcher resultMatcher = resultPattern.matcher(result);

                while (resultMatcher.find()) {
                    result = resultMatcher.group(1) + ".class";
                }
            }

            String resultMethod = result.contains("Exception") ? "thenThrow" : "thenReturn";

            final String replacement = "when(" + methodCall + ")." + resultMethod + "(" + result + ");\n\t\t";

            updatedContent = updatedContent.replaceAll(
                    Pattern.quote(matcher.group()),
                    Matcher.quoteReplacement(replacement)
            );
        }

        return updatedContent.trim();
    }

    public static String updateVerificationStatements(String content) {

        String updatedContent = content;

        final String regex = "\\bnew Verifications\\(\\)\\s?\\{\\{?([^}]*)\\}\\}?\\;?";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {

            final String verifyStatements = updateToVerifyStatements(matcher.group(1));

            updatedContent = updatedContent.replaceAll(
                    Pattern.quote(matcher.group()),
                    Matcher.quoteReplacement(verifyStatements)
            );
        }

        return updatedContent.trim();
    }

    private static String updateToVerifyStatements(String content) {

        String updatedContent = content;

        final String regex = "\\s*(.*);\\s*times\\s?=\\s?(.*);";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {

            final String methodCall = matcher.group(1);
            String times = matcher.group(2);

            String classToVerify = "";
            String classMethodToVerify = "";

            if (times.contains("0")) {
                times = "never()";
            } else {
                times = "times(" + times + ")";
            }

            final String classMethodRegex = "([A-Za-z\\d]*)\\.(.*)";
            final Pattern classMethodPattern = Pattern.compile(classMethodRegex);
            final Matcher classMethodMatcher = classMethodPattern.matcher(methodCall);

            while (classMethodMatcher.find()) {

                classToVerify = classMethodMatcher.group(1);
                classMethodToVerify = classMethodMatcher.group(2);
            }

            final String replacement = "verify(" + classToVerify + ", " + times + ")." + classMethodToVerify + ";\n\t\t";

            updatedContent = updatedContent.replaceAll(
                    Pattern.quote(matcher.group()),
                    Matcher.quoteReplacement(replacement)
            );
        }

        return updatedContent.trim();
    }
}
