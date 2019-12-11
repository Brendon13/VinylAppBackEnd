package com.vinyl.controller;

import com.vinyl.config.JwtAuthenticationEntryPoint;
import com.vinyl.config.JwtRequestFilter;
import com.vinyl.config.JwtTokenUtil;
import com.vinyl.model.User;
import com.vinyl.repository.UserRepository;
import com.vinyl.service.CartService;
import com.vinyl.service.ItemService;
import com.vinyl.service.JwtUserDetailsService;
import com.vinyl.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(CommonController.class)
@WebAppConfiguration
@ContextConfiguration
public class TokenCreationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUserDetailsService userDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @MockBean
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private UserService userService;

    @MockBean
    private CartService cartService;

    @MockBean
    private ItemService itemService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private WebApplicationContext context;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(jwtRequestFilter).build();
    }

    @Test
    public void userLoginTestWithParametersOk() throws Exception{
        userLogin("kovacs.brendon@gmail.com", "123456").andExpect(status().isOk());
    }

    @Test
    public void userLoginTestWithNoUsername() throws Exception{
        userLogin("", "123456").andExpect(status().isBadRequest());
    }

    @Test
    public void userLoginTestWithNoPassword() throws Exception{
        userLogin("kovacs.brendon@gmail.com", "").andExpect(status().isBadRequest());
    }

    /*@Test
    public void Test() throws Exception{
        mockMvc.perform(post("/VinylStore/api/users/login").with(authentication("user")))

    }*/


    private ResultActions userLogin(String username, String password) throws Exception {
        return mockMvc.perform(
                post("/VinylStore/api/users/login")
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")
                        .contentType("application/json")
                        .characterEncoding("utf-8"));
    }
}
