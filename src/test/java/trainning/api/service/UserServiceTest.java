package trainning.api.service;

import org.junit.jupiter.api.Test;
import trainning.api.exception.InvalidPasswordException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserServiceTest {
    @Test
    public void validatePasswordTest() throws NoSuchMethodException {
        UserService userService = new UserService();
        Method method = UserService.class.getDeclaredMethod("validatePassword", String.class);
        method.setAccessible(true);

        invokeAndExpectException(method, userService, "",InvalidPasswordException.class);
        invokeAndExpectException(method, userService, "1234567890",InvalidPasswordException.class);
        invokeAndExpectException(method, userService, "abc1234567",InvalidPasswordException.class);
        invokeAndExpectException(method, userService, "Abc1234567",InvalidPasswordException.class);
        invokeAndExpectException(method, userService, "Ab1_",InvalidPasswordException.class);
        assertDoesNotThrow(() -> method.invoke(userService, "Abc123456789_"));
    }

    private void invokeAndExpectException(Method method, UserService userService, String password, Class<? extends Throwable> expectedException) {
        assertThrows(expectedException, () -> {
            try {
                method.invoke(userService, password);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
    }
}
