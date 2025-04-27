package com.nguyenmoclam.tutorialyoutubemadesimple.data.model

/**
 * Represents the different states of API key validation.
 */
enum class ApiKeyValidationState {
    /**
     * Initial state - no validation has been attempted
     */
    NOT_VALIDATED,
    
    /**
     * Currently validating the API key
     */
    VALIDATING,
    
    /**
     * API key has been validated and is valid
     */
    VALID,
    
    /**
     * API key has been validated but is invalid (wrong format or rejected by the API)
     */
    INVALID,
    
    /**
     * API key format is invalid (doesn't match expected pattern)
     */
    INVALID_FORMAT,
    
    /**
     * API key format is valid but rejected by the API service
     */
    INVALID_NETWORK,
    
    /**
     * Network error occurred during validation (couldn't reach API)
     */
    NETWORK_ERROR,
    
    /**
     * An error occurred during validation (general error case)
     */
    ERROR
} 