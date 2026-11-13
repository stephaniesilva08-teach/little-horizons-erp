package com.littlehorizons.erp.model;

/** Safe account data stored for the lifetime of an authenticated console session. */
public record SessionUser(int id, String username, String displayName,
                          AccessRole role, boolean active) {
}
