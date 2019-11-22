package com.vinyl.controller;

import com.vinyl.config.JwtTokenUtil;
import com.vinyl.model.JwtRequest;
import com.vinyl.model.JwtResponse;
import com.vinyl.model.User;
import com.vinyl.service.JwtUserDetailsService;
import com.vinyl.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping(value="/")
public class MainController {
    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @RequestMapping(value = "/addUser", method = RequestMethod.POST)
    public ResponseEntity<Object> addUser(@Valid @RequestBody User user){
        if (userService.findByEmailAddress(user.getEmailAddress()) == null) {
            userService.save(user);
            return new ResponseEntity<>("User Created!", HttpStatus.OK);
        }
        else return new ResponseEntity<>("Email already in use!", HttpStatus.FORBIDDEN);
    }

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

        final String token = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(token));
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

    @DeleteMapping(value = "/delete")
    public ResponseEntity<Object> deleteUser(@RequestBody User user) {
        userService.delete(userService.findByEmailAddress(user.getEmailAddress()));
        return new ResponseEntity<>("User Deleted!", HttpStatus.NO_CONTENT);
    }
}
