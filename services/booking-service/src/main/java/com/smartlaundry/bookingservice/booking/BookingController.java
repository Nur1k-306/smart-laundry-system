package com.smartlaundry.bookingservice.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/bookings")
    public BookingService.BookingResponse create(@Valid @RequestBody CreateBookingRequest request) {
        return bookingService.createBooking(request.machineId());
    }

    @DeleteMapping("/bookings/{id}")
    public void cancel(@PathVariable UUID id) {
        bookingService.cancelBooking(id);
    }

    @GetMapping("/bookings/me")
    public List<BookingService.BookingResponse> myBookings() {
        return bookingService.getMyBookings();
    }

    @GetMapping("/internal/bookings/{id}")
    public BookingService.BookingResponse internalBooking(@PathVariable UUID id) {
        return bookingService.findForInternal(id);
    }

    public record CreateBookingRequest(@NotNull UUID machineId) {
    }
}
