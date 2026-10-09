# Original Requirements

Captured here for reference.

## Business Rules

*   There are 3 types of tickets i.e. INFANT, CHILD, and ADULT.
*   The ticket prices are based on the type of ticket (see table below).
*   The ticket purchaser declares how many and what type of tickets they want to buy.
*   Multiple tickets can be purchased at any given time.
*   Only a maximum of 25 tickets that can be purchased at a time.
*   An INFANT does not pay for a ticket and are not allocated a seat. They will be sitting on an ADULT lap.
*   CHILD and INFANT tickets cannot be purchased without purchasing an ADULT ticket.

    | Ticket Type | Price  |
    |-------------|--------|
    | INFANT      | £0     |
    | CHILD       | £17.50 |
    | ADULT       | £25.99 |

*   There is an existing `PaymentService` responsible for taking payments.
*   There is an existing `SeatReservationService` responsible for reserving seats.

## Assumptions

*   All accounts with an id greater than zero are valid. They also have sufficient funds to pay for any number of tickets.
*   The `PaymentService` implementation is an external provider with no defects.
*   You do not need to worry about how the actual payment happens or integrating that service.
*   The payment will always go through once a payment request has been made to the `PaymentService`.
*   The `SeatReservationService` implementation is an external provider with no defects.
*   You do not need to worry about how the seat reservation algorithm works or integrating that service.
*   The seats will always be reserved once a reservation request has been made to the `SeatReservationService`.

## Detail

*   Calculates the correct amount for the requested tickets and makes a payment request to the `PaymentService`.
*   Calculates the correct number of seats to reserve and makes a seat reservation request to the `SeatReservationService`.
*   Rejects any invalid ticket purchase requests.
*   Appropriate error handling and testing is implemented for the 'CinemaTicketsController' referring to all business rules and rejected ticket requests.
