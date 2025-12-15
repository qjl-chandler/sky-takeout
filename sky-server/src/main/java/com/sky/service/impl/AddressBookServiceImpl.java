package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class AddressBookServiceImpl implements AddressBookService {
    @Autowired
    AddressBookMapper addressBookMapper;

    @Override
    public void insert(AddressBook addressBook) {
        Long id = BaseContext.getCurrentId();
        addressBook.setUserId(id);
        addressBook.setIsDefault(0);
        addressBookMapper.insert(addressBook);

    }



    @Override
    public void update(AddressBook addressBook) {
        addressBookMapper.update(addressBook);

    }

    @Override
    public void deleteById(Long id) {
        addressBookMapper.deleteById(id);
    }


    @Override
    public List<AddressBook> list(AddressBook addressBook) {

        return addressBookMapper.list(addressBook);


    }

    //涉及两步骤 第一步 修改之前的默认地址 (将此用户所有地址修改为非默认） 第二步修改当前默认地址
    @Override
    @Transactional
    public void updateDefaultAddressBook(AddressBook addressBook) {
        AddressBook upddateAddressBook = new AddressBook();
        Long currentId = BaseContext.getCurrentId();
        upddateAddressBook.setUserId(currentId);
        upddateAddressBook.setIsDefault(0);
        addressBookMapper.updateBatchbyUserId(upddateAddressBook);

        addressBook.setIsDefault(1);
        addressBookMapper.update(addressBook);
    }

    @Override
    public AddressBook selectById(Long id) {
        AddressBook addressBook = addressBookMapper.selectById(id);
        return addressBook;
    }
}
