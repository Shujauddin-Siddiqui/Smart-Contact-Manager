package com.smart.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.validation.Valid;
@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	UserRepository userRepository;
	
	@GetMapping({"/home", "/"})
	public String homePageHandler(Model model){
		model.addAttribute("title", "Home-Smart Contact Manager");
		return "home";
	}
	
	@GetMapping("/about")
	public ResponseEntity<Void> aboutPageHandler(@RequestParam("name") String name){
		//model.addAttribute("title", "About-Smart Contact Manager");
		System.out.println("Received name: " + name);
		return ResponseEntity.ok().build();
	}
	
	@GetMapping("/signup")
	public String signupPageHandler(Model model){
		model.addAttribute("title", "Register-Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}
	@PostMapping("/do_register")
	public String registerUserHandler(@Valid @ModelAttribute("user") User user, BindingResult result, @RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model) {
		try {
			if(!agreement) {
				System.out.println("You have not agreed to the terms and conditions");
				throw new Exception("You have not agreed to the terms and conditions");
			}
			if(result.hasErrors()) {
				model.addAttribute("user", user);
				System.out.println("Coffee");
				System.out.println("Validation error: "+result.toString());
				return "signup";
			}
			user.setRole("Role_User");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			// Saving User to DB
			User userReturned = this.userRepository.save(user);		
			
			System.out.println("Agreement: "+ agreement);
			System.out.println(userReturned);
			
			//if everything went fine add blank user and show successful message
			model.addAttribute("user", new User());
			
			model.addAttribute("message", new Message("Successfully registered !!", "alert-success"));
			return "signup";
		} catch (Exception e) {
			System.out.println("Inside catch block");
			e.printStackTrace();
			model.addAttribute("user", user);
			model.addAttribute("message", new Message("Something get wrong !! "+e.getMessage(), "alert-danger"));
			System.out.println("Inside catch block part2");
			return "signup";
		}
	}
	
	//handler for custom login page 
	@RequestMapping("/signin")
	public String customLoginHandler(Model model) {
		model.addAttribute("title", "This is the title of custom login page");
		return "login";
	}
}
