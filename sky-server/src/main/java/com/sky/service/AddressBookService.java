package com.sky.service;


import com.sky.entity.AddressBook;

import java.util.List;

public interface AddressBookService {
    void insert(AddressBook addressBook);

    

    void update(AddressBook addressBook);

    void deleteById(Long id);

    

    List<AddressBook> list(AddressBook addressBook);

    void updateDefaultAddressBook(AddressBook addressBook);

    AddressBook selectById(Long id);
}
