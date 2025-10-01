package com.helios.cctv.controller;

import com.helios.cctv.dto.ApiResponse;
import com.helios.cctv.dto.cctv.CctvApiDTO;
import com.helios.cctv.dto.cctv.request.GetCctvRequest;
import com.helios.cctv.entity.cctv.Cctv;
import com.helios.cctv.service.CctvService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin("http://localhost:4000/")
@RequestMapping("/cctv")
public class CctvController {

    private final CctvService cctvService;

    @GetMapping("/view")
    public ResponseEntity<ApiResponse<?>> getCctv(@ModelAttribute GetCctvRequest getCctvRequest) {
        ApiResponse<?> apiResponse = cctvService.findInBoundsAsDto(getCctvRequest);
        if (apiResponse.isSuccess()) {
            return ResponseEntity.ok(apiResponse);
        } else {
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Void>> saveCctv(@RequestBody GetCctvRequest getCctvRequest){
        ApiResponse<Void> apiResponse = cctvService.saveCctv(getCctvRequest);
        if (apiResponse.isSuccess()) {
            return ResponseEntity.ok(apiResponse);
        } else {
            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CctvApiDTO>>> search(@RequestParam String search){
        ApiResponse<List<CctvApiDTO>> response = cctvService.search(search);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/one")
    public ResponseEntity<ApiResponse<CctvApiDTO>> getCctv(@RequestParam Long id){
        ApiResponse<CctvApiDTO> result = cctvService.findInBoundsOneAsDto(id);
        return result.isSuccess() ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }


}
