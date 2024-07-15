package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import com.smart.entities.Contact;
import com.smart.entities.User;
@Component
public interface ContactRepository extends JpaRepository<Contact, Integer>{
	//pagination ....
	//pageable will have two information current page and contacts per page
	//current page 
	//contact per page - 5
	@Query("from Contact as c where c.user.id = :userId" )
	public Page<Contact> findContactsByUser(@Param("userId")int userId, Pageable pageable);
	
	//search contact query
	public List<Contact> findByNameContainingAndUser(String name, User user);
}
