package com.financeapp.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.financeapp.model.User;

/**
 * 测试AuthController类
 * 测试用户注册、登录和其他身份验证操作
 */
public class AuthControllerTest {

    private AuthController authController;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_FULLNAME = "Test User";
    private static final String TEST_EMAIL = "test@example.com";
    
    @TempDir
    Path tempDir;
    
    /**
     * 每次测试前的设置
     * 创建临时数据目录并初始化AuthController
     */
    @BeforeEach
    public void setUp() throws IOException {
        // 创建临时数据目录
        Path dataDir = tempDir.resolve("data");
        Files.createDirectories(dataDir);
        
        // 设置系统属性以使用临时目录
        System.setProperty("user.dir", tempDir.toString());
        
        // 初始化AuthController
        authController = new AuthController();
    }
    
    /**
     * 每次测试后的清理
     */
    @AfterEach
    public void tearDown() {
        // 重置系统属性
        System.clearProperty("user.dir");
    }
    
    /**
     * 测试使用有效数据注册用户
     */
    @Test
    public void testRegisterValidUser() throws IOException {
        // 注册新用户
        User user = authController.register(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, TEST_EMAIL);
        
        // 验证用户是否创建成功且数据正确
        assertNotNull(user);
        assertEquals(TEST_USERNAME, user.getUsername());
        assertEquals(TEST_FULLNAME, user.getFullName());
        assertEquals(TEST_EMAIL, user.getEmail());
        assertNotNull(user.getPasswordHash());
        assertNotNull(user.getId());
        assertNotNull(user.getCreatedAt());
    }
    
    /**
     * 测试使用空用户名注册
     */
    @Test
    public void testRegisterEmptyUsername() {
        // 尝试使用空用户名注册
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authController.register("", TEST_PASSWORD, TEST_FULLNAME, TEST_EMAIL);
        });
        
        // 验证异常消息
        assertTrue(exception.getMessage().contains("Username cannot be empty"));
    }
    
    /**
     * 测试使用过短密码注册
     */
    @Test
    public void testRegisterShortPassword() {
        // 尝试使用过短密码注册
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authController.register(TEST_USERNAME, "12345", TEST_FULLNAME, TEST_EMAIL);
        });
        
        // 验证异常消息
        assertTrue(exception.getMessage().contains("Password must be at least 6 characters"));
    }
    
    /**
     * 测试使用重复用户名注册
     */
    @Test
    public void testRegisterDuplicateUsername() throws IOException {
        // 注册第一个用户
        authController.register(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, TEST_EMAIL);
        
        // 尝试使用相同用户名注册
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            authController.register(TEST_USERNAME, "different123", "Another User", "another@example.com");
        });
        
        // 验证异常消息
        assertTrue(exception.getMessage().contains("Username already exists"));
    }
    
    /**
     * 测试成功登录
     */
    @Test
    public void testLoginSuccess() throws IOException {
        // 注册用户
        authController.register(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, TEST_EMAIL);
        
        // 使用正确凭据登录
        boolean loginResult = authController.login(TEST_USERNAME, TEST_PASSWORD);
        
        // 验证登录成功
        assertTrue(loginResult);
        assertTrue(authController.isLoggedIn());
        assertNotNull(authController.getCurrentUser());
        assertEquals(TEST_USERNAME, authController.getCurrentUser().getUsername());
        assertNotNull(authController.getCurrentUser().getLastLoginAt());
    }
    
    /**
     * 测试使用错误密码登录
     */
    @Test
    public void testLoginWrongPassword() throws IOException {
        // 注册用户
        authController.register(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, TEST_EMAIL);
        
        // 尝试使用错误密码登录
        boolean loginResult = authController.login(TEST_USERNAME, "wrongpassword");
        
        // 验证登录失败
        assertFalse(loginResult);
        assertFalse(authController.isLoggedIn());
        assertNull(authController.getCurrentUser());
    }
    
    /**
     * 测试使用不存在的用户名登录
     */
    @Test
    public void testLoginNonExistentUser() throws IOException {
        // 尝试使用不存在的用户名登录
        boolean loginResult = authController.login("nonexistentuser", TEST_PASSWORD);
        
        // 验证登录失败
        assertFalse(loginResult);
        assertFalse(authController.isLoggedIn());
        assertNull(authController.getCurrentUser());
    }
    
    /**
     * 测试登出功能
     */
    @Test
    public void testLogout() throws IOException {
        // 注册并登录用户
        authController.register(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, TEST_EMAIL);
        authController.login(TEST_USERNAME, TEST_PASSWORD);
        
        // 验证用户已登录
        assertTrue(authController.isLoggedIn());
        
        // 登出
        authController.logout();
        
        // 验证用户已登出
        assertFalse(authController.isLoggedIn());
        assertNull(authController.getCurrentUser());
    }
    
    /**
     * 测试更改密码功能
     */
    @Test
    public void testChangePassword() throws IOException {
        // 注册并登录用户
        authController.register(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, TEST_EMAIL);
        authController.login(TEST_USERNAME, TEST_PASSWORD);
        
        // 更改密码
        String newPassword = "newpassword123";
        boolean changeResult = authController.changePassword(TEST_PASSWORD, newPassword);
        
        // 验证密码更改成功
        assertTrue(changeResult);
        
        // 登出
        authController.logout();
        
        // 尝试使用旧密码登录
        boolean oldLoginResult = authController.login(TEST_USERNAME, TEST_PASSWORD);
        assertFalse(oldLoginResult);
        
        // 尝试使用新密码登录
        boolean newLoginResult = authController.login(TEST_USERNAME, newPassword);
        assertTrue(newLoginResult);
    }
    
    /**
     * 测试使用错误的当前密码更改密码
     */
    @Test
    public void testChangePasswordWrongCurrentPassword() throws IOException {
        // 注册并登录用户
        authController.register(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, TEST_EMAIL);
        authController.login(TEST_USERNAME, TEST_PASSWORD);
        
        // 尝试使用错误的当前密码更改密码
        boolean changeResult = authController.changePassword("wrongpassword", "newpassword123");
        
        // 验证密码更改失败
        assertFalse(changeResult);
    }
    
    /**
     * 测试更新个人资料功能
     */
    @Test
    public void testUpdateProfile() throws IOException {
        // 注册并登录用户
        authController.register(TEST_USERNAME, TEST_PASSWORD, TEST_FULLNAME, TEST_EMAIL);
        authController.login(TEST_USERNAME, TEST_PASSWORD);
        
        // 更新个人资料
        String newFullName = "Updated Name";
        String newEmail = "updated@example.com";
        authController.updateProfile(newFullName, newEmail);
        
        // 验证个人资料已更新
        assertEquals(newFullName, authController.getCurrentUser().getFullName());
        assertEquals(newEmail, authController.getCurrentUser().getEmail());
    }
} 