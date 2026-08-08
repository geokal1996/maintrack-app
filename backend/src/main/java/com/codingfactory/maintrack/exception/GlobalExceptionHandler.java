package com.codingfactory.maintrack.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

// To "@RestControllerAdvice" leei sto Spring: "pare ta lathi apo OLOUS tous controllers
// kai perase ta apo edo, gia na apantame panta me to idio, kathara format".
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Otan petaxtei ResourceNotFoundException opoudipote sto app -> 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        ApiError error = new ApiError(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Otan ta dedomena pou estile o xristis den perasoun to validation (p.x. leipei to title) -> 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex) {
        ApiError error = new ApiError(HttpStatus.CONFLICT.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // p.x. lathos typos arxeiou sto Excel import, i arxeio pou den mporei na diavastei -> 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Otan to username/password sto login den einai sosta.
    // PROSOXI: to minima einai epitides GENIKO - den leme "den yparxei autos o xristis",
    // giati auto tha epetrepe se kapoion na anakalypsei poia usernames yparxoun.
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
        ApiError error = new ApiError(HttpStatus.UNAUTHORIZED.value(), "Λάθος username ή κωδικός");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // Otan o logariasmos yparxei alla einai ANENERGOS (p.x. nea eggrafi pou perimenei
    // egkrisi apo epopti). Edo to sygkekrimeno minima einai xrisimo, oxi epikindyno -
    // o xristis kserei idi to password tou.
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> handleDisabled(DisabledException ex) {
        ApiError error = new ApiError(HttpStatus.FORBIDDEN.value(),
                "Ο λογαριασμός σου δεν έχει ενεργοποιηθεί ακόμα. Ζήτησε από έναν επόπτη να τον εγκρίνει.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // Otan o syndedemenos xristis EXEI men login, alla den exei to dikaioma gia AUTI ti sygkekrimeni energeia
    // (p.x. enas SUPERVISOR pou prospathei na ftiaxei allon SUPERVISOR) -> 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        ApiError error = new ApiError(HttpStatus.FORBIDDEN.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // Otan i vasi apporiptei tin egrafi giati paravazei UNIQUE constraint
    // (p.x. dio xristes me to idio username, i dio mihanes me ton idio kodiko) -> 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ApiError error = new ApiError(HttpStatus.CONFLICT.value(),
                "Τα στοιχεία υπάρχουν ήδη (π.χ. το username ή ο κωδικός μηχανής χρησιμοποιείται από άλλη εγγραφή)");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
