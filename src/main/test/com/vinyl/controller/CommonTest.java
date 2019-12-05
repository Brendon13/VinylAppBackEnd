//package com.vinyl.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.gson.Gson;
//import com.vinyl.config.JwtTokenUtil;
//import com.vinyl.model.User;
//import com.vinyl.model.UserRole;
//import com.vinyl.repository.UserRepository;
//import com.vinyl.service.CartService;
//import com.vinyl.service.ItemService;
//import com.vinyl.service.JwtUserDetailsService;
//import com.vinyl.service.UserService;
//import org.hamcrest.Matchers;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Mockito;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//
//import static net.bytebuddy.matcher.ElementMatchers.is;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.hamcrest.Matchers.hasSize;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@WebMvcTest(Common.class)
//public class CommonTest {
//    private static final String BASE_URL = "/heroes/";
//
//    private static final Logger LOG = LoggerFactory.getLogger(CommonTest.class);
//
//    @Autowired
//    JdbcTemplate jdbcTemplate;
//
//    @Autowired
//    private MockMvc mvc;
//
//    // Used for converting heroes to/from JSON
//    private ObjectMapper mapper = new ObjectMapper();
//
//
//    @Before
//    public void initTests() {
//        // Always start from known state, in this case 1 row in hero table.
//        jdbcTemplate.execute("delete from user; insert into user(firstName) values ('Clark');" +
//                "insert into user(lastName) values ('Kent'); insert into user(emailAddress) values ('superman@gmail.com');" +
//                "insert into user(password) values ('123456');");
//    }
//
//    @Test
//    public void contextLoads() {
//        assertThat(jdbcTemplate).isNotNull();
//        assertThat(mvc).isNotNull();
//    }
//
//    @Test
//    public void shouldStartWithOneSuperheroSuperman() throws Exception {
//        MvcResult result = invokeAllHeroes()
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(1)))
//                .andExpect(jsonPath("$[0].firstName", Matchers.is("Clark")))
//                .andReturn();
//
//        User[] users = fromJsonResult(result, User[].class);
//        LOG.debug("Superman's id: {}", users[0].getId());
//    }
//
//    /**
//     * Convert JSON Result to object.
//     *
//     * @param result
//     *            The contents
//     * @param tClass
//     *            The expected object class
//     * @return The result as class.
//     *
//     * @throws Exception
//     *             if you got any of the above wrong.
//     */
//    <T> T fromJsonResult(MvcResult result, Class<T> tClass) throws Exception {
//        return this.mapper.readValue(result.getResponse().getContentAsString(), tClass);
//    }
//
//    /**
//     * Convert object to JSON bytes.
//     *
//     * @param object
//     *            The object to JSONify
//     * @return byte array with JSON representation
//     * @throws Exception
//     */
//    private byte[] toJson(Object object) throws Exception {
//        return this.mapper.writeValueAsString(object).getBytes();
//    }
//
///*
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private UserRepository userRepository;
//
//    @MockBean
//    private JwtUserDetailsService userDetailsService;
//
//    @MockBean
//    private BCryptPasswordEncoder bCryptPasswordEncoder;
//
//    @MockBean
//    private JwtTokenUtil jwtTokenUtil;
//
//    @MockBean
//    private UserService userService;
//
//    @MockBean
//    private CartService cartService;
//
//    @MockBean
//    private ItemService itemService;
//
//    @MockBean
//    private AuthenticationManager authenticationManager;
//
//    @Test
//    public void testAddUser() throws Exception {
//
//        User user = mock(User.class);
//        UserRole role = mock(UserRole.class);
//        role.setId(1L);
//        role.setRoles("customer");
//
//        doThrow(IllegalArgumentException.class).when(user).setFirstName(null);
//        doThrow(IllegalArgumentException.class).when(user).setLastName(null);
//        doThrow(IllegalArgumentException.class).when(user).setEmailAddress(null);
//        doThrow(IllegalArgumentException.class).when(user).setPassword(null);
//        doThrow(IllegalArgumentException.class).when(user).setUserRole(null);
//
//        doAnswer((i) -> {
//            System.out.println("User setFirstName Argument = " + i.getArgument(0));
//            assertEquals("Brendon", i.getArgument(0));
//            return null;
//        }).when(user).setFirstName(anyString());
//
//        when(user.getFirstName()).thenReturn("Brendon");
//
//        doAnswer((i) ->{
//            System.out.println("User setLastName Argument = " + i.getArgument(0));
//            assertEquals("Kovacs", i.getArgument(0));
//            return null;
//        }).when(user).setLastName(anyString());
//
//        when(user.getLastName()).thenReturn("Kovacs");
//
//        doAnswer((i) ->{
//            System.out.println("User setEmailAddress Argument = " + i.getArgument(0));
//            assertEquals("kovacs.brendon223@gmail.com", i.getArgument(0));
//            return null;
//        }).when(user).setEmailAddress(anyString());
//
//        when(user.getEmailAddress()).thenReturn("kovacs.brendon223@gmail.com");
//
//        doAnswer((i) ->{
//            System.out.println("User setPassword Argument = " + i.getArgument(0));
//            assertEquals("$2a$10$GVTnofdX9dK/1xZXRv3hNuGy2Jw1mV56/cl2untyOlqYdRoVYB2X2", i.getArgument(0));
//            return null;
//        }).when(user).setPassword(anyString());
//
//        when(user.getPassword()).thenReturn("$2a$10$GVTnofdX9dK/1xZXRv3hNuGy2Jw1mV56/cl2untyOlqYdRoVYB2X2");
//
//        doAnswer((i) ->{
//            System.out.println("User setUserRole Argument = " + i.getArgument(0));
//            assertEquals(role, i.getArgument(0));
//            return null;
//        }).when(user).setUserRole(role);
//
//        when(user.getUserRole()).thenReturn(role);
//
//        assertThrows(IllegalArgumentException.class, () -> user.setFirstName(null));
//        assertThrows(IllegalArgumentException.class, () -> user.setLastName(null));
//        assertThrows(IllegalArgumentException.class, () -> user.setEmailAddress(null));
//        assertThrows(IllegalArgumentException.class, () -> user.setPassword(null));
//        assertThrows(IllegalArgumentException.class, () -> user.setUserRole(null));
//
//        user.setFirstName("Brendon");
//        user.setLastName("Kovacs");
//        user.setEmailAddress("kovacs.brendon223@gmail.com");
//        user.setPassword("$2a$10$GVTnofdX9dK/1xZXRv3hNuGy2Jw1mV56/cl2untyOlqYdRoVYB2X2");
//        user.setUserRole(new UserRole(1L,"customer"));
//        assertEquals("Brendon", user.getFirstName());
//        assertEquals("Kovacs", user.getLastName());
//        assertEquals("kovacs.brendon223@gmail.com", user.getEmailAddress());
//        assertEquals("$2a$10$GVTnofdX9dK/1xZXRv3hNuGy2Jw1mV56/cl2untyOlqYdRoVYB2X2", user.getPassword());
//        assertEquals(role, user.getUserRole());
//    }
//
//*/
//        /*@Test
//        public void testAddUserPost() throws Exception {
//        User objToAdd = new User();
//        objToAdd.setId(10L);
//        objToAdd.setEmailAddress("kovacs.brendon223@gmail.com");
//        objToAdd.setPassword(bCryptPasswordEncoder.encode("123456"));
//        objToAdd.setFirstName("Brendon");
//        objToAdd.setLastName("Kovacs");
//        objToAdd.setUserRole(new UserRole(1L,"customer"));
//
//        Gson gson = new Gson();
//        String jsonString = gson.toJson(objToAdd);
//
//        when(userRepository.save(any(User.class))).thenReturn(objToAdd);
//
//        //Mockito.when(userRepository.save(Mockito.anyString(),Mockito.any(Common.class))).thenReturn(objToAdd);
//
//        mockMvc.perform(post("/VinylStore/api/users")
//                .contentType(MediaType.APPLICATION_JSON_UTF8).content(jsonString))
//                .andExpect(status().isForbidden());
//
//                //.andExpect(status().isOk());
//            /mockMvc.perform(post("/VinylStore/api/users")
//                    .contentType(MediaType.APPLICATION_JSON_UTF8).content(jsonString))
//
//        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
//        verify(userRepository, times(1)).save(userArgumentCaptor.capture());
//        verifyNoMoreInteractions(userRepository);
//
//        User userArgument = userArgumentCaptor.getValue();
//        assertEquals(is(10L), userArgument .getId());
//        assertEquals(is("kovacs.brendon2@gmail.com"), userArgument.getEmailAddress());
//        assertEquals(is("123456"), userArgument .getPassword());
//        assertEquals(is("Brendon"), userArgument .getFirstName());
//        assertEquals(is("Kovacs"), userArgument .getLastName());
//    }*/
//}