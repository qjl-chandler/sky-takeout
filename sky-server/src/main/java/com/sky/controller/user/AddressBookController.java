package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/user/addressBook")
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    @PostMapping()
    public Result addAddressBook(@RequestBody AddressBook addressBook) {
        addressBookService.insert(addressBook);
        return Result.success();
    }

    //注意只能展示自己的list
    @GetMapping("/list")
    public Result<List<AddressBook>> list() {
        AddressBook addressBook = new AddressBook();
        Long currentId = BaseContext.getCurrentId();
        addressBook.setUserId(currentId);
        List<AddressBook> addressBookList = addressBookService.list(addressBook);
        return Result.success(addressBookList);
    }

    @GetMapping("/default")
    public Result<AddressBook> defaultList() {
        AddressBook addressBook = new AddressBook();
        addressBook.setIsDefault(1);
        addressBook.setUserId(BaseContext.getCurrentId());
        List<AddressBook> list = addressBookService.list(addressBook);

        if (list != null && list.size() == 1) {
            return Result.success(list.get(0));
        }

        return Result.error("没有查询到默认地址");

    }

    @PutMapping()
    public Result updateAddressBook(@RequestBody AddressBook addressBook) {
        addressBookService.update(addressBook);
        return Result.success();
    }

    @DeleteMapping()
    public Result deleteAddressBook(Long id) {
        addressBookService.deleteById(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<AddressBook> getAddressBook1(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.selectById(id);
        return Result.success(addressBook);
    }

    @PutMapping("/default")
    public Result updateDefaultAddressBook(@RequestBody AddressBook addressBook) {
        addressBookService.updateDefaultAddressBook(addressBook);
        return Result.success();

    }


}
