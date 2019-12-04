package com.vinyl.controller;

import com.vinyl.model.*;
import com.vinyl.service.ItemService;
import com.vinyl.service.OrderService;
import com.vinyl.service.StatusService;
import com.vinyl.service.UserService;
import io.swagger.annotations.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@RestController
@RequestMapping(value="/VinylStore/api")
@Api(value="ManagerController", description="Manager operations for the Vinyl Store")
public class Manager {
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private StatusService statusService;

    @ApiOperation(value = "Add vinyl to store", response = Iterable.class)
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

    @ApiOperation(value = "Delete vinyl from store", response = Iterable.class)
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

    @ApiOperation(value = "Update vinyl", response = Iterable.class)
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

    @ApiOperation(value = "Update order", response = Iterable.class)
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

    @ApiOperation(value = "Get all customers", response = Iterable.class)
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

    @ApiOperation(value = "Get order form an user", response = Iterable.class)
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

    private Boolean passwordMatch(JwtRequest request){
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

    private Boolean loggedIn(JwtAddVinylRequest request){
        return userService.findByEmailAddress(request.getUsername()) != null && passwordMatch(request);
    }

    private Boolean loggedIn(JwtOrderRequest request){
        return userService.findByEmailAddress(request.getUsername()) != null && passwordMatch(request);
    }
}
