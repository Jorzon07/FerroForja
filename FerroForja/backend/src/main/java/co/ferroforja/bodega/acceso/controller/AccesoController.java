package co.ferroforja.bodega.acceso.controller;

import co.ferroforja.bodega.acceso.dto.request.LoginRequest;
import co.ferroforja.bodega.acceso.dto.response.AccesoResponse;
import co.ferroforja.bodega.acceso.service.AccesoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AccesoController {

    private final AccesoService accesoService;

    public AccesoController(AccesoService accesoService) {
        this.accesoService = accesoService;
    }

    @GetMapping("/csrf")
    public ResponseEntity<java.util.Map<String, String>> getCsrf(CsrfToken token, HttpServletRequest request) {
        if (token == null) {
            token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        }
        if (token == null) {
            token = (CsrfToken) request.getAttribute("_csrf");
        }
        String tokenVal = token != null ? token.getToken() : "";
        String headerName = token != null ? token.getHeaderName() : "X-XSRF-TOKEN";
        return ResponseEntity.ok(java.util.Map.of(
            "token", tokenVal,
            "headerName", headerName
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<AccesoResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        AccesoResponse response = accesoService.login(request, httpRequest, httpResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
        accesoService.logout(httpRequest);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AccesoResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(accesoService.getOperarioHabilitado(userDetails.getUsername()));
    }
}
