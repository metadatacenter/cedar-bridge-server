package org.metadatacenter.cedar.bridge.resources.extauth;

/** A semantic fact, without a template- or instance-specific save decision. */
public record AuthorityVerification(String status, String code, String message, String iri) {}
