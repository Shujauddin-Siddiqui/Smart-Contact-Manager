package com.smart.controllers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
@EnableMethodSecurity
public class UserController {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	// method to add common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String name = principal.getName();
		System.out.println("Name obtained from principal : " + name);

		// Getting user detail from DB using username.

		User user = userRepository.getUserByUserName(name);
		System.out.println(name);
		model.addAttribute("user", user);
	}

	// dash-board home
	@PreAuthorize("hasAuthority('Role_User')")
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	// open add contacts form handler
	@GetMapping("/add-contact")
	public String openAddContactFormHandler(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// processing the contact form
	@PostMapping("/process-contact")
	public String processContactHandler(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {
		try {
			String name = principal.getName();

			User user = userRepository.getUserByUserName(name);

			// Processing and uploading the file....

			if (file.isEmpty()) {
				// if file is empty than show proper message..
				System.out.println("File is empty");
				contact.setImageUrl("default.png");
			} else {
				// upload the file to folder and update the name to contact..
				contact.setImageUrl(file.getOriginalFilename());
				File savedFile = new ClassPathResource("static/images").getFile();
				Path path = Paths.get(savedFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				System.out.println("Absoulute path of image saved for contact: " + path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Images is uploaded for contact");
			}

			contact.setUser(user);

			user.getContacts().add(contact);
			userRepository.save(user);

			System.out.println("Contact added to Database");
			// Success message
			session.setAttribute("message", new Message("Your contact is added !! Add more", "success"));

			System.out.println("Data = " + contact);
		} catch (Exception e) {
			// error message
			session.setAttribute("message", new Message("Something went wrong !! Try again", "danger"));

			System.out.println("Error inside processContactHandler : " + e.getMessage());
			e.printStackTrace();
		}
		return "normal/add_contact_form";
	}

	// show contacts handler
	// per page = 5[n]
	// current page = [page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
		m.addAttribute("title", "Show User Contacts");
		// getting the contact list

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);

		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";
	}

	// showing particular contact details

	@RequestMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model m, Principal principal) {
		System.out.println("CID: " + cId);

		try {
			Optional<Contact> contactOptional = this.contactRepository.findById(cId);
			Contact contact = contactOptional.get();

			String name = principal.getName();
			User user = userRepository.getUserByUserName(name);
			if (user.getId() == contact.getUser().getId()) {
				m.addAttribute("contact", contact);
				m.addAttribute("title", contact.getName());
			}

		} catch (Exception e) {
			System.out.println("Contact not found for cId: " + cId);
		}

		return "normal/contact_details";

	}

	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Principal principal, HttpSession session) {
		try {
			Contact contact = this.contactRepository.findById(cId).get();
			String name = principal.getName();
			User user = userRepository.getUserByUserName(name);
			if (user.getId() == contact.getUser().getId()) {
				// Unlinking the user from contact table so it can be deleted.
//				contact.setUser(null);
//				this.contactRepository.delete(contact);
				
				user.getContacts().remove(contact);
				
				this.userRepository.save(user);
				
				// Removing the image from folder before deleting the contact
				// TBD
				System.out.println("Contact with cId: " + cId + " is deleted successfully");
				session.setAttribute("message", new Message("Contact Deleted Successfully.....", "success"));
			}
		} catch (Exception e) {
			System.out.println("Contact not found for cId: " + cId);
		}

		return "redirect:/user/show-contacts/0";
	}

	// Updating a particular contact
	@PostMapping("/update_contact/{cId}")
	public String updateContact(@PathVariable("cId") Integer cId, Principal principal, HttpSession session, Model m) {
		m.addAttribute("title", "Update Contact");
		try {
			Contact contact = this.contactRepository.findById(cId).get();
			String name = principal.getName();
			User user = userRepository.getUserByUserName(name);
			if (user.getId() == contact.getUser().getId()) {
				m.addAttribute("contact", contact);
			}
		} catch (Exception e) {
			System.out.println("Contact not found for cId: " + cId);
		}
		return "/normal/update_contact";
	}

	// Processing the update contact form
	@PostMapping("process-update-contact")
	public String processUpdateCotact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model m, HttpSession session, Principal principal) {
		try {
			Contact oldContact = this.contactRepository.findById(contact.getcId()).get();
			if (!file.isEmpty()) {
				// file rewrite
				//delete old
				File deleteFile = new ClassPathResource("static/images").getFile();
				File temp = new File(deleteFile, oldContact.getImageUrl());
				temp.delete();
				
				// update new
				File savedFile = new ClassPathResource("static/images").getFile();
				Path path = Paths.get(savedFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				System.out.println("Absoulute path of image saved for contact: " + path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImageUrl(file.getOriginalFilename());
			} else {
				contact.setImageUrl(oldContact.getImageUrl());
			}
			User user = userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Your contact is updated...", "success"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	//User profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model, Principal principal) {
		model.addAttribute("title", "Profile Page");
		User user = this.userRepository.getUserByUserName(principal.getName());
		model.addAttribute("user", user);
		return "/normal/profile";
	}
}
