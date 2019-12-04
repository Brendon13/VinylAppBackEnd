package com.vinyl.controller;

import com.vinyl.config.JwtTokenUtil;
import com.vinyl.model.*;
import com.vinyl.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value="/VinylStore/api")
@Api(value="CommonController", description="Common operations for the Vinyl Store")
public class Common {
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @ApiOperation(value = "Create User", response = Iterable.class)
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

    @ApiOperation(value = "Create Manager", response = Iterable.class)
    @PostMapping(value = "/managers")
    public ResponseEntity<Object> addManager(@Valid @RequestBody User user) {
        if (userService.findByEmailAddress(user.getEmailAddress()) == null) {
            userService.saveManager(user);
            return new ResponseEntity<>("Manager Created!", HttpStatus.OK);
        } else return new ResponseEntity<>("Email already in use!", HttpStatus.FORBIDDEN);
    }

    @ApiOperation(value = "User Login", response = Iterable.class)
    @PostMapping(value = "/users/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

        final String token = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(token));
    }

    @ApiOperation(value = "Retrieve all vinyls", response = Iterable.class)
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

    @ApiOperation(value = "Delete User", response = Iterable.class)
    @DeleteMapping(value = "/users/{user_id}")
    public @ResponseBody ResponseEntity<Object> deleteUser(@RequestBody JwtRequest deletionRequest, @PathVariable Long user_id) {

        if (loggedIn(deletionRequest)) {
            userService.delete(userService.findById(user_id));
            return new ResponseEntity<>("User Deleted!", HttpStatus.NO_CONTENT);
        } else return new ResponseEntity<>("User or password not correct", HttpStatus.FORBIDDEN);
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