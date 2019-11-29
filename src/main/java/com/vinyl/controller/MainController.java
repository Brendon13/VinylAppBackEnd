package com.vinyl.controller;

import com.sun.org.apache.xpath.internal.operations.Bool;
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
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Controller
@RequestMapping(value="/VinylStore/api")
public class MainController {
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private StatusService statusService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    private static final Gson gson = new Gson();

    @RequestMapping(value = "/users", method = RequestMethod.POST)
    public ResponseEntity<Object> addUser(@Valid @RequestBody User user) {
        if (userService.findByEmailAddress(user.getEmailAddress()) == null) {
            userService.save(user);

            Cart cart = new Cart();
            cart.setUser(user);
            cartService.save(cart);

            return new ResponseEntity<>("User Created!", HttpStatus.OK);
        } else return new ResponseEntity<>("Email already in use!", HttpStatus.FORBIDDEN);
    }

    @RequestMapping(value = "/managers", method = RequestMethod.POST)
    public ResponseEntity<Object> addManager(@Valid @RequestBody User user) {
        if (userService.findByEmailAddress(user.getEmailAddress()) == null) {
            userService.saveManager(user);
            return new ResponseEntity<>("Manager Created!", HttpStatus.OK);
        } else return new ResponseEntity<>("Email already in use!", HttpStatus.FORBIDDEN);
    }

    @RequestMapping(value = "/users/login", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

        final String token = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(token));
    }

    @DeleteMapping(value = "/users/{user_id}")
    public @ResponseBody ResponseEntity<Object> deleteUser(@RequestBody JwtRequest deletionRequest, @PathVariable Long user_id) {

        if (loggedIn(deletionRequest)) {
            userService.delete(userService.findById(user_id));
            return new ResponseEntity<>("User Deleted!", HttpStatus.NO_CONTENT);
        } else return new ResponseEntity<>("User or password not correct", HttpStatus.FORBIDDEN);
    }

    @RequestMapping(value = "/customer/cart/detail", method = RequestMethod.GET)
    public ResponseEntity<?> getCart(@RequestBody JwtRequest getCartRequest) throws JSONException {

        Cart cart = cartService.findByUserId(userService.findByEmailAddress(getCartRequest.getUsername()).getId());
        List<CartItem> cartItem = cartItemService.findByCartId(cart.getId());
        double totalPrice = 0;

        for(int i = 0; i< (long) cartItem.size(); i++){
            totalPrice+= cartItem.get(i).getQuantity()*cartItem.get(i).getItem().getPrice();
        }

        JSONObject json = new JSONObject();
        json.put("Number of items", cartItem.size());
        json.put("Total cost", totalPrice);

        JSONArray json3 = new JSONArray();

        for(int i = 0; i< (long) cartItem.size(); i++){
            JSONObject json2 = new JSONObject();
            json2.put("Name", cartItem.get(i).getItem().getName());//item.get(i).getName());
            json2.put("Description", cartItem.get(i).getItem().getDescription());//item.get(i).getDescription());
            json2.put("Price", cartItem.get(i).getItem().getPrice());//item.get(i).getPrice());
            json2.put("Quantity", cartItem.get(i).getQuantity());
            json3.put(json2);
        }

        json.put("Items", json3);

        if (loggedIn(getCartRequest))
            if(cartItem.size() == 0)
                return new ResponseEntity<>("No items in cart", HttpStatus.OK);
            else return new ResponseEntity<>(json.toString(), HttpStatus.OK);
        else return new ResponseEntity<>("You are not logged in", HttpStatus.FORBIDDEN);
    }

    @RequestMapping(value = "/vinyls/cart/{vinyl_id}", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<?> addVinyl(@Valid @RequestBody JwtRequest addVinylRequest, @PathVariable Long vinyl_id){
        Item item = itemService.findById(vinyl_id).get();
        Cart cart = cartService.findByUserId(userService.findByEmailAddress(addVinylRequest.getUsername()).getId());
        CartItem cartItem = new CartItem();

        if(loggedIn(addVinylRequest)){
            if(addVinylRequest.getQuantity()<=0)
                return new ResponseEntity<>("Quantity can't be negative or zero!", HttpStatus.FORBIDDEN);
            else if(addVinylRequest.getQuantity()>item.getQuantity()){
                return new ResponseEntity<>("Quantity too big!", HttpStatus.FORBIDDEN);
            }
                else{
                    cartItem.setItem(item);
                    cartItem.setCart(cart);
                    cartItem.setQuantity(addVinylRequest.getQuantity());
                    cartItemService.save(cartItem);

                    return ResponseEntity.ok("Item added to cart!");
            }
        }
        else return new ResponseEntity<>("You are not logged in", HttpStatus.FORBIDDEN);
    }

    @DeleteMapping(value = "/users/{user_id}/cart/{item_id}")
    public @ResponseBody ResponseEntity<?> removeVinyl(@RequestBody JwtRequest deleteVinylRequest, @PathVariable Long user_id, @PathVariable Long item_id){
        Cart cart = cartService.findByUserId(user_id);
        List<CartItem> cartItem = cartItemService.findByCartId(cart.getId());

        if(loggedIn(deleteVinylRequest)){
            cartItem.forEach(cItem -> {
                if(cItem.getItem().getId().equals(item_id))
                    cartItemService.delete(cItem);
            });

            return ResponseEntity.ok("Item deleted from cart!");
        }
        else return new ResponseEntity<>("You are not logged in", HttpStatus.FORBIDDEN);
    }

    @RequestMapping(value = "/{user_id}/orders", method = RequestMethod.PUT)
    public @ResponseBody ResponseEntity<?> placeOrder(@RequestBody JwtRequest placeOrderRequest, @PathVariable Long user_id){
        List <Boolean> quantityResponse = new ArrayList<>();
        Order order = new Order();

        Cart cart = cartService.findByUserId(user_id);
        List<CartItem> cartItem = cartItemService.findByCartId(cart.getId());

        double totalPrice = 0;

        for(int i = 0; i< (long) cartItem.size(); i++){
            totalPrice+= cartItem.get(i).getQuantity()*cartItem.get(i).getItem().getPrice();
        }

        if(loggedIn(placeOrderRequest)){
            cartItem.forEach(cItem -> {
                if(cItem.getQuantity()>cItem.getItem().getQuantity())
                    quantityResponse.add(false);
            });

            if(quantityResponse.contains(false))
            return new ResponseEntity<>("No more items availabile", HttpStatus.BAD_REQUEST);
            else{
                cartItem.forEach(cItem -> cItem.getItem().setQuantity(cItem.getItem().getQuantity()-cItem.getQuantity()));
                order.setUser(userService.findById(user_id));
                order.setCreatedAt(new Date());
                order.setUpdatedAt(new Date());
                order.setStatus(statusService.findById(1L));
                order.setTotal_price(totalPrice);
                orderService.save(order);

                return ResponseEntity.ok("Order placed!");
            }
        }
        else return new ResponseEntity<>("You are not logged in", HttpStatus.FORBIDDEN);
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

    private Boolean loggedIn(JwtRequest request){
        return userService.findByEmailAddress(request.getUsername()) != null && passwordMatch(request);
    }
}