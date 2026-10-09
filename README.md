# Cinema Tickets Service

A service for making cinema bookings: taking payments and reserving seats.

A copy of the original requirements is [captured separately](docs/original-requirements.md) for
reference.

TODO: add an overview of the service/code layout here, and some build & run notes (I ran out of time)

## Coding Test Notes

### Approach

1.  Rather than shoehorn everything into CinemaTicketsServiceImpl, I've separated out the validation
    and calculations into their own classes (SRP).
2.  As I expect the requirements for these validations and calculations will change and we may need
    to support multiple methods, I've used interfaces and supplied these initial versions as one
    implementation of them (Strategy pattern).
3.  The pricing and rules may need to come from an external source at some point, a database or
    something dynamic, so I've put the current pricing (and some rule details) in config as a nod
    towards that, and it keeps magic price numbers out of the code at least.
4.  As instructed, the code in the `domain` package is unmodified, other than BookingConfirmation
    which I made a minor addition to, which the specified constraints allowed.
5.  My usual approach is to not modify existing code unless I need to anyway.  The task specified
    here did not include improving or fixing any existing code, though if I spot something that
    looks wrong I might raise a new ticket for it (in JIRA or whatever system we're using) and add 
    a TODO comment in the code.  (e.g. see cinema-tickets.yaml)
6.  For enhancements to an existing project, I also try to follow the particular style of the
    existing project, so it stays as consistent as possible throughout.
7.  `BookingConfirmationTest` is also in the `domain` package, so I've assumed the do-not-modify
    instruction applies to that too.  I probably wouldn't add tests there anyway because they would
    just be checking behaviour of the language (java `record`).
8.  Where I use a dependency, I try to make sure I add it to the pom explicitly, rather than rely on
    it being available transitively, so I've added hamcrest and mockito for example.
9.  I was tempted to use some more libraries (e.g. see the TODO about Guava in Preconditions) but I
    would need to check what's been approved first, so I've not introduced anything new here.
10.  All combinations of the specified Business Rules are tested in `BookingValidatorImplTest`,
     though most are also tested in `CinemaTicketsControllerTest`.  It seemed worth testing in both
     places.
11.  I would normally use a feature branch and pull request, but thought I'd better keep it simple
     here and commit to `main`, just in case.
12.  I didn't manage to get your pre-commit checks working fully, so I've just tried to follow what
     seems like the expected thing to do.

### Additional Assumptions

1.  "Only a maximum of 25 tickets that can be purchased at a time" means a maximum of 25 tickets,
    combined across a single booking request (including INFANT tickets).
2.  Language is always British English, currency is always GBP.
3.  Each ADULT can only accommodate one INFANT at most.
4.  The business rules effectively state there must always be at least one ADULT ticket requested.
5.  The number of INFANT tickets is limited to the number of ADULT tickets. (Implied by the business
    rules.)
6.  There's no separate limit on the number of CHILD tickets per ADULT ticket.  (Some venues might
    say something like a maximum of 4 CHILDren per ADULT.)
7.  If there are multiple TicketRequests with the same TicketType, that's treated as valid and to be
    handled gracefully by merging the counts, rather than responding with an error.
8.  Invalid requests should receive a 400 Bad Request http response.
9.  The ticket types, prices and seat requirements per ticket type are all fixed.
10.  The constraint to do with extending BookingConfirmation "in a non-breaking manner" I took to
     mean, the existing code in BookingConfirmationTest must continue to pass, untouched.
11.  The OpenAPI spec defines seatCount and totalCost as required on BookingConfirmations, between
     that and the 'non-breaking' hint in the supplied Constraints, I've assumed that is a
     requirement and added them to BookingConfirmation (in a non-breaking way).
12.  No exception handling or transactions are required for the two external services, as they
     always work.

### Further Work

1.  Add some security.  The current design seems particularly vulnerable, e.g. the user is
    unauthenticated and supplies an accountId in the request!
2.  Documentation (see TODO above - I ran out of time)
3.  If the `domain` package becomes modifiable, it might be better to use something more flexible
    than an enum for the TicketTypes.
4.  It might be better to make `CinemaTicketsService.purchaseTickets` accept a request object
    instead of specific params.  And including a request ID (UUID) so the service can check the
    request is new.
5.  Handle issues arising from use of the payment and seat allocation services, with transactions
    and rollback (e.g. if the cinema doesn't have enough space, the customer payment didn't work,
    external services being unavailable etc).
6.  It might be good to use properties or something in error responses to identify the field(s) the
    problem arises from - for UX purposes (e.g. to highlight the relevant textbox), and if languages
    other than English are to be supported then the error type may need a code or identifier too.
7.  I expect the service may need to have support added for multiple Cinema venues, screens, films,
    specific film screenings, pricing structures, specific seat allocations, receipt and transaction
    identifiers, and possibly other things like upgrades and discounts, subscription services, etc.
8.  Use configuration/data more flexibly from a database or some other dynamic source.
