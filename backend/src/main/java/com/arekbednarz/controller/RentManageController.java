package com.arekbednarz.controller;

import com.arekbednarz.enums.RentTimeEnum;
import com.arekbednarz.model.entity.User;
import com.arekbednarz.service.IRentalService;
import com.arekbednarz.service.IStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/rental")
public class RentManageController {
	private final IRentalService rentalService;
	private final IStoreService storeService;

	@Autowired
	public RentManageController(
		IRentalService rentalService,
		IStoreService storeService) {
		this.rentalService = rentalService;
		this.storeService = storeService;
	}

	@PutMapping(path = "/{id}/rent", produces = "application/json")
	@PreAuthorize("hasAuthority('rent:manage')")
	public ResponseEntity rentMovie(
		@PathVariable(value = "id") final Long id,
		@RequestParam(value = "dueDate") final RentTimeEnum rentTime) {
		User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		rentalService.rentMovie(id, currentUser, rentTime.getDateTime());
		return ResponseEntity.noContent().build();
	}

	@PutMapping(path = "/{id}/return", produces = "application/json")
	@PreAuthorize("hasAuthority('rent:manage')")
	public ResponseEntity returnMovie(
		@PathVariable(value = "id") final Long id) {
		User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		rentalService.returnMovie(id, currentUser);
		return ResponseEntity.noContent().build();
	}

	@GetMapping(path = "user/{userid}/{page}/{size}/rentals", produces = "application/json")
	@PreAuthorize("hasAuthority('user:create')")
	public ResponseEntity getUserRentals(
		@PathVariable(value = "userid") final Long userid,
		@PathVariable(value = "page") final int page,
		@PathVariable(value = "size") final int size,
		@RequestParam(value = "returned") final boolean returned) {
		return ResponseEntity.ok().body(rentalService.getRentalsPaged(userid, returned, page, size));
	}

	@GetMapping(path = "self/{page}/{size}/rentals", produces = "application/json")
	@PreAuthorize("hasAuthority('self:manage')")
	public ResponseEntity getUserRentals(
		@PathVariable(value = "page") final int page,
		@PathVariable(value = "size") final int size,
		@RequestParam(value = "returned") final boolean returned) {
		User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return ResponseEntity.ok().body(rentalService.getRentalsPaged(currentUser.getId(), returned, page, size));
	}

	@GetMapping(path = "self/reminder", produces = "application/json")
	@PreAuthorize("hasAuthority('self:manage')")
	public ResponseEntity getUserRentals() {
		User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		var reminders = storeService.getAll().stream()
			.filter(reminder -> reminder.getUserEmail().equals(currentUser.getEmail()))
			.toList();

		return ResponseEntity.ok().body(reminders);
	}
}
