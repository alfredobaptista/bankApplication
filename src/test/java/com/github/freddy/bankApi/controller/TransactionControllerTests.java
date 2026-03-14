package com.github.freddy.bankApi.controller;

import com.github.freddy.bankApi.dto.request.TransferRequest;
import com.github.freddy.bankApi.dto.response.TransferResponse;
import com.github.freddy.bankApi.entity.Account;
import com.github.freddy.bankApi.factory.AccountFactory;
import com.github.freddy.bankApi.factory.TransferRequestFactory;
import com.github.freddy.bankApi.factory.TransferResponseFactory;
import com.github.freddy.bankApi.factory.UserFactory;
import com.github.freddy.bankApi.service.TransactionService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class TransactionControllerTests {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @Nested
    class Transfer{
        @Test
        public void transferSuccess(){
            //ARRANGE
            TransferRequest request = TransferRequestFactory.createTransferRequest();
            MockHttpServletRequest req = new MockHttpServletRequest();
            Account account = AccountFactory.createAccount(UserFactory.createUser());
            //TransferResponse response = TransferResponseFactory.createTransferResponse(account);

            //doReturn(response).when(transactionService).transfer(request, UserFactory.createUser().getId());
            //ACT
            //var response = transactionController.transfer(request, req);
            //ASSERT
        }
    }
}
