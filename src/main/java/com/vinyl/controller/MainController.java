package com.vinyl.controller;

import com.vinyl.config.JwtTokenUtil;
import com.vinyl.model.*;
import com.vinyl.service.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.google.gson.Gson;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value="/")
public class MainController {
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    private static final Gson gson = new Gson();

    @RequestMapping(value = "/addUser", method = RequestMethod.POST)
    public ResponseEntity<Object> addUser(@Valid @RequestBody User user) {
        if (userService.findByEmailAddress(user.getEmailAddress()) == null) {
            userService.save(user);
            return new ResponseEntity<>("User Created!", HttpStatus.OK);
        } else return new ResponseEntity<>("Email already in use!", HttpStatus.FORBIDDEN);
    }

    @RequestMapping(value = "/addManager", method = RequestMethod.POST)
    public ResponseEntity<Object> addManager(@Valid @RequestBody User user) {
        if (userService.findByEmailAddress(user.getEmailAddress()) == null) {
            userService.saveManager(user);
            return new ResponseEntity<>("Manager Created!", HttpStatus.OK);
        } else return new ResponseEntity<>("Email already in use!", HttpStatus.FORBIDDEN);
    }

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

        final String token = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(token));
    }

    @DeleteMapping(value = "/delete")
    public ResponseEntity<Object> deleteUser(@RequestBody JwtRequest deletionRequest) {

        if (userService.findByEmailAddress(deletionRequest.getUsername()) != null && passwordMatch(deletionRequest)) {
            userService.delete(userService.findByEmailAddress(deletionRequest.getUsername()));
            return new ResponseEntity<>("User Deleted!", HttpStatus.NO_CONTENT);
        } else return new ResponseEntity<>("User or password not correct", HttpStatus.FORBIDDEN);
    }

    @RequestMapping(value = "/customer/cart/details", method = RequestMethod.GET)
    public ResponseEntity<?> getCart(@RequestBody JwtRequest getCartRequest) throws JSONException {

        Cart cart = cartService.findByUserId(userService.findByEmailAddress(getCartRequest.getUsername()).getId());
        List<OrderItem> orderItem = orderItemService.findByOrderId(cart.getOrder().getId());
        Order order = cart.getOrder();
        List<Item> item = new ArrayList<>();
        double totalPrice = 0;
        long itemCount = 0;

        itemCount += orderItem.size();

        orderItem.forEach(oItem -> item.add(oItem.getItem()));

        for(int i = 0; i< (long) orderItem.size(); i++){
            totalPrice+=orderItem.get(i).getQuantity()*item.get(i).getPrice();
        }

        order.setTotal_price(totalPrice);

        JSONObject json = new JSONObject();
        json.put("Number of items", itemCount);
        json.put("Total cost", totalPrice);

        JSONArray json3 = new JSONArray();

        for(int i = 0; i< (long) orderItem.size(); i++){
            JSONObject json2 = new JSONObject();
            json2.put("Name", item.get(i).getName());
            json2.put("Description", item.get(i).getDescription());
            json2.put("Price", item.get(i).getPrice());
            json2.put("Quantity", orderItem.get(i).getQuantity());
            json3.put(json2);
        }

        json.put("Items", json3);

        if (userService.findByEmailAddress(getCartRequest.getUsername()) != null && passwordMatch(getCartRequest)) {
            if(cart.getOrder() == null)
            return new ResponseEntity<>("No items in cart", HttpStatus.OK);
            else return new ResponseEntity<>(json.toString(), HttpStatus.OK);
        } else return new ResponseEntity<>("You are not logged in", HttpStatus.FORBIDDEN);
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

    private Boolean passwordMatch(JwtRequest request){
        return bCryptPasswordEncoder.matches(request.getPassword(),
                userService.findByEmailAddress(request.getUsername()).getPassword());
    }
}