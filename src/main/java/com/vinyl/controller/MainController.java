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
import org.springframework.web.bind.annotation.*;
import com.google.gson.Gson;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
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

    @PostMapping(value = "/users")
    public ResponseEntity<Object> addUser(@Valid @RequestBody User user) {
        if (userService.findByEmailAddress(user.getEmailAddress()) == null) {
            userService.save(user);

            Cart cart = new Cart();
            cart.setUser(user);
            cartService.save(cart);

            return new ResponseEntity<>("User Created!", HttpStatus.OK);
        } else return new ResponseEntity<>("Email already in use!", HttpStatus.FORBIDDEN);
    }

    @PostMapping(value = "/managers")
    public ResponseEntity<Object> addManager(@Valid @RequestBody User user) {
        if (userService.findByEmailAddress(user.getEmailAddress()) == null) {
            userService.saveManager(user);
            return new ResponseEntity<>("Manager Created!", HttpStatus.OK);
        } else return new ResponseEntity<>("Email already in use!", HttpStatus.FORBIDDEN);
    }

    @PostMapping(value = "/users/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

        final String token = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(token));  //"User logged in!"
    }

    @DeleteMapping(value = "/users/{user_id}")
    public @ResponseBody ResponseEntity<Object> deleteUser(@RequestBody JwtRequest deletionRequest, @PathVariable Long user_id) {

        if (loggedIn(deletionRequest)) {
            userService.delete(userService.findById(user_id));
            return new ResponseEntity<>("User Deleted!", HttpStatus.NO_CONTENT);
        } else return new ResponseEntity<>("User or password not correct", HttpStatus.FORBIDDEN);
    }

    @GetMapping(value = "/customer/cart/detail")
    public ResponseEntity<?> getCart(@RequestParam String email, @RequestParam Long user_id) throws JSONException {
        if (userService.findByEmailAddress(email) != null && userService.findById(user_id) != null) {

            Cart cart = cartService.findByUserId(user_id);
            List<CartItem> cartItem = cartItemService.findByCartId(cart.getId());
            double totalPrice = 0;

            for (int i = 0; i < (long) cartItem.size(); i++) {
                totalPrice += cartItem.get(i).getQuantity() * cartItem.get(i).getItem().getPrice();
            }

            JSONObject json = new JSONObject();
            json.put("Number of items", cartItem.size());
            json.put("Total cost", totalPrice);

            JSONArray json3 = new JSONArray();

            for (int i = 0; i < (long) cartItem.size(); i++) {
                JSONObject json2 = new JSONObject();
                json2.put("Name", cartItem.get(i).getItem().getName());
                json2.put("Description", cartItem.get(i).getItem().getDescription());
                json2.put("Price", cartItem.get(i).getItem().getPrice());
                json2.put("Quantity", cartItem.get(i).getQuantity());
                json3.put(json2);
            }

            json.put("Items", json3);


            if (cartItem.size() == 0)
                return new ResponseEntity<>("No items in cart", HttpStatus.OK);
            else return new ResponseEntity<>(json.toString(), HttpStatus.OK);
        }
        else return new ResponseEntity<>("You are not logged in", HttpStatus.FORBIDDEN);
    }

    @PostMapping(value = "/vinyls/cart/{vinyl_id}")
    public @ResponseBody ResponseEntity<?> addVinyl(@Valid @RequestBody JwtVinylRequest addVinylRequest, @PathVariable Long vinyl_id){
        if(loggedIn(addVinylRequest)){
            try {
        Item item = itemService.findById(vinyl_id);
        Cart cart = cartService.findByUserId(userService.findByEmailAddress(addVinylRequest.getUsername()).getId());
        CartItem cartItem = new CartItem();


               if (addVinylRequest.getQuantity() <= 0)
                   return new ResponseEntity<>("Quantity can't be negative or zero!", HttpStatus.FORBIDDEN);
               else if (addVinylRequest.getQuantity() > item.getQuantity()) {
                   return new ResponseEntity<>("Quantity too big!", HttpStatus.FORBIDDEN);
               } else {
                   cartItem.setItem(item);
                   cartItem.setCart(cart);
                   cartItem.setQuantity(addVinylRequest.getQuantity());
                   cartItemService.save(cartItem);

                   return ResponseEntity.ok("Item added to cart!");
               }
           }
           catch (NullPointerException e){
               return new ResponseEntity<>("Quantity can't be null!", HttpStatus.FORBIDDEN);
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

    @PutMapping(value = "/{user_id}/orders")
    public @ResponseBody ResponseEntity<?> placeOrder(@RequestBody JwtRequest placeOrderRequest, @PathVariable Long user_id){
        if(loggedIn(placeOrderRequest)){
        List <Boolean> quantityResponse = new ArrayList<>();
        Order order = new Order();

        Cart cart = cartService.findByUserId(user_id);
        List<CartItem> cartItem = cartItemService.findByCartId(cart.getId());

        double totalPrice = 0;

        for(int i = 0; i< (long) cartItem.size(); i++){
            totalPrice+= cartItem.get(i).getQuantity()*cartItem.get(i).getItem().getPrice();
        }


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

    @PostMapping(value = "/vinyls")
    public ResponseEntity<?> addVinyl(@RequestBody JwtAddVinylRequest vinylRequest){
        Item item = new Item();
        if(loggedIn(vinylRequest) && userService.findByEmailAddress(vinylRequest.getUsername()).getUserRole().getId() == 2){
            try {
                if (vinylRequest.getStock() < 0)
                    return new ResponseEntity<>("Stock can't be negative!", HttpStatus.FORBIDDEN);
                else if(vinylRequest.getCost() < 0)
                    return new ResponseEntity<>("Cost can't be negative!", HttpStatus.FORBIDDEN);
                    else if(itemService.findByName(vinylRequest.getName()) == null){
                        item.setQuantity(vinylRequest.getStock());
                        item.setDescription(vinylRequest.getDescription());
                        item.setName(vinylRequest.getName());
                        item.setPrice(vinylRequest.getCost());
                        itemService.save(item);

                        return ResponseEntity.ok("Item inserted!");
                        }
                        else return new ResponseEntity<>("Item already exists!", HttpStatus.FORBIDDEN);
            }
            catch (NullPointerException e){
                return new ResponseEntity<>("Stock can't be null!", HttpStatus.FORBIDDEN);
            }
        }
        else return new ResponseEntity<>("You are not logged in or not a manager!", HttpStatus.FORBIDDEN);
    }

    @DeleteMapping(value = "/vinyls/{vinyl_id}")
    public @ResponseBody ResponseEntity<?> deleteVinyl(@RequestBody JwtRequest vinylRequest, @PathVariable Long vinyl_id) {
        if(loggedIn(vinylRequest) && userService.findByEmailAddress(vinylRequest.getUsername()).getUserRole().getId() == 2){
            if(itemService.findById(vinyl_id) != null){
                itemService.delete(itemService.findById(vinyl_id));
                return new ResponseEntity<>("Item Deleted!", HttpStatus.NO_CONTENT);
            }
            else{
                return new ResponseEntity<>("Item does not exist!", HttpStatus.NOT_FOUND);
            }
        }
        else return new ResponseEntity<>("You are not logged in or not a manager!", HttpStatus.FORBIDDEN);
    }

    @PutMapping(value = "/vinyls/{vinyl_id}")
    public @ResponseBody ResponseEntity<?> updateVinyl(@RequestBody JwtAddVinylRequest vinylRequest, @PathVariable Long vinyl_id){
        Item item = new Item();
        if(loggedIn(vinylRequest) && userService.findByEmailAddress(vinylRequest.getUsername()).getUserRole().getId() == 2){
            try {
                if (vinylRequest.getStock() < 0)
                    return new ResponseEntity<>("Stock can't be negative!", HttpStatus.FORBIDDEN);
                else if(vinylRequest.getCost() < 0)
                    return new ResponseEntity<>("Cost can't be negative!", HttpStatus.FORBIDDEN);
                else if(itemService.findById(vinyl_id) != null){
                    item.setQuantity(vinylRequest.getStock());
                    item.setDescription(vinylRequest.getDescription());
                    item.setName(vinylRequest.getName());
                    item.setPrice(vinylRequest.getCost());
                    item.setId(vinyl_id);
                    itemService.save(item);

                    return ResponseEntity.ok("Item updated!");
                }
                else return new ResponseEntity<>("Item doesn't exists!", HttpStatus.FORBIDDEN);
            }
            catch (NullPointerException e){
                return new ResponseEntity<>("Stock can't be null!", HttpStatus.FORBIDDEN);
            }
        }
        else return new ResponseEntity<>("You are not logged in or not a manager!", HttpStatus.FORBIDDEN);
    }

    @PutMapping(value = "/orders/{order_id}")
    public @ResponseBody ResponseEntity<?> updateOrder(@RequestBody JwtOrderRequest orderRequest, @PathVariable Long order_id){
        Order order;
        if(loggedIn(orderRequest) && userService.findByEmailAddress(orderRequest.getUsername()).getUserRole().getId() == 2){
            if(orderService.findById(order_id) != null){
                if(orderRequest.getStatus() == 1 || orderRequest.getStatus() == 2){
                    order = orderService.findById(order_id);
                    order.setStatus(statusService.findById(orderRequest.getStatus()));
                    orderService.save(order);

                    return ResponseEntity.ok("Order status changed!");
                }
                else return new ResponseEntity<>("Status is not a valid Id!", HttpStatus.FORBIDDEN);
            }
            else return new ResponseEntity<>("Order doesn't exist!", HttpStatus.NOT_FOUND);
        }
        else return new ResponseEntity<>("You are not logged in or not a manager!", HttpStatus.FORBIDDEN);
    }

    @GetMapping(value = "/vinyls")
    public ResponseEntity<?> getVinyl(@RequestParam String email, @RequestParam Long user_id) throws JSONException{
        if(userService.findByEmailAddress(email) != null && userService.findById(user_id) != null) {
            List<Item> items = itemService.findAll();

            JSONObject json = new JSONObject();
            JSONArray json3 = new JSONArray();

            for(int i = 0; i< (long) items.size(); i++){
                JSONObject json2 = new JSONObject();
                json2.put("Id", items.get(i).getId());
                json2.put("Name", items.get(i).getName());
                json2.put("Description", items.get(i).getDescription());
                json2.put("Price", items.get(i).getPrice());
                json2.put("Quantity", items.get(i).getQuantity());
                json3.put(json2);
            }

            json.put("Vinyls", json3);

            return new ResponseEntity<>(json.toString(), HttpStatus.OK);
        }
        else return new ResponseEntity<>("You are not logged in!", HttpStatus.FORBIDDEN);
    }

    @GetMapping(value = "/customers")
    public ResponseEntity<?> getCustomers(@RequestParam String email, @RequestParam Long user_id) throws JSONException{
        if(userService.findByEmailAddress(email) != null && userService.findById(user_id) != null && userService.findByEmailAddress(email).getUserRole().getId() == 2){
            UserRole userRole = new UserRole((long)1,"customer");
            List<User> users = userService.findByUserRole(userRole);

            JSONObject json = new JSONObject();
            JSONArray json3 = new JSONArray();

            for(int i = 0; i< (long) users.size(); i++){
                JSONObject json2 = new JSONObject();
                json2.put("Email", users.get(i).getEmailAddress());
                json2.put("First Name", users.get(i).getFirstName());
                json2.put("Last Name", users.get(i).getLastName());
                json3.put(json2);
            }

            json.put("Customers", json3);

            return new ResponseEntity<>(json.toString(), HttpStatus.OK);
        }
        else return new ResponseEntity<>("You are not logged in or not a manager!", HttpStatus.FORBIDDEN);
    }

    @GetMapping(value = "/users/{user_id}/orders")
    public ResponseEntity<?> getUserOrder(@RequestParam String email, @RequestParam Long manager_id, @PathVariable Long user_id) throws JSONException {
        if(userService.findByEmailAddress(email) != null && userService.findById(manager_id) != null && userService.findByEmailAddress(email).getUserRole().getId() == 2){
            try {
                if (userService.findById(user_id).getEmailAddress().isEmpty()) {
                    return new ResponseEntity<>("User id doesn't exist!", HttpStatus.BAD_REQUEST);
                } else {

                    List<Order> orders = orderService.findByUserId(user_id);
                    JSONObject json = new JSONObject();
                    JSONArray json3 = new JSONArray();

                    for (int i = 0; i < (long) orders.size(); i++) {
                        JSONObject json2 = new JSONObject();
                        json2.put("Cost", orders.get(i).getTotal_price());
                        json2.put("Order Date", orders.get(i).getCreatedAt());
                        json2.put("Status", orders.get(i).getStatus().getStatus());
                        json3.put(json2);
                    }

                    json.put("Orders", json3);

                    return new ResponseEntity<>(json.toString(), HttpStatus.OK);
                }
            }
            catch (EntityNotFoundException e){
                return new ResponseEntity<>("User id doesn't exist!", HttpStatus.BAD_REQUEST);
            }
        }
        else return new ResponseEntity<>("You are not logged in or not a manager!", HttpStatus.FORBIDDEN);
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

    private Boolean passwordMatch(JwtVinylRequest request){
        return bCryptPasswordEncoder.matches(request.getPassword(),
                userService.findByEmailAddress(request.getUsername()).getPassword());
    }

    private Boolean passwordMatch(JwtAddVinylRequest request){
        return bCryptPasswordEncoder.matches(request.getPassword(),
                userService.findByEmailAddress(request.getUsername()).getPassword());
    }

    private Boolean passwordMatch(JwtOrderRequest request){
        return bCryptPasswordEncoder.matches(request.getPassword(),
                userService.findByEmailAddress(request.getUsername()).getPassword());
    }

    private Boolean loggedIn(JwtRequest request){
        return userService.findByEmailAddress(request.getUsername()) != null && passwordMatch(request);
    }

    private Boolean loggedIn(JwtVinylRequest request){
        return userService.findByEmailAddress(request.getUsername()) != null && passwordMatch(request);
    }

    private Boolean loggedIn(JwtAddVinylRequest request){
        return userService.findByEmailAddress(request.getUsername()) != null && passwordMatch(request);
    }

    private Boolean loggedIn(JwtOrderRequest request){
        return userService.findByEmailAddress(request.getUsername()) != null && passwordMatch(request);
    }
}