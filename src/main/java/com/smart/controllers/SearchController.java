package com.smart.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;

@RestController
public class SearchController {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	@GetMapping("/search/{query}")
	public ResponseEntity<?> search(@PathVariable("query") String query, Principal principal) {
		System.out.println("Contact search query = " + query);
		String currentUserName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(currentUserName);
		List<Contact> searchedContacts = this.contactRepository.findByNameContainingAndUser(query, currentUser);
		return ResponseEntity.ok(searchedContacts);
	}

}
