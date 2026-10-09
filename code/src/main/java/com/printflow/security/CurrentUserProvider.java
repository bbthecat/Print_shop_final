package com.printflow.security;

public interface CurrentUserProvider {

    Long getCurrentUserId();

    // true ถ้าคนที่ login อยู่เป็น STAFF หรือ ADMIN
    boolean isStaffOrAdmin();
}
