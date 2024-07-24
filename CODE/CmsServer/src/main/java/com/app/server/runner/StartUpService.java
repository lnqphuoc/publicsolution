package com.app.server.runner;

import com.app.server.enums.ResponseStatus;
import com.app.server.manager.DataManager;
import com.app.server.manager.ProductManager;
import com.app.server.service.BaseService;
import com.app.server.utils.AppUtils;
import com.app.server.utils.JsonUtils;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.utils.DateTimeUtils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class StartUpService implements ApplicationRunner {

    private BaseService baseService;

    @Autowired
    public void setBaseService(BaseService baseService) {
        this.baseService = baseService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LogUtil.printDebug("-START LOAD DATA");
        this.baseService.dataManager.loadData();
        LogUtil.printDebug("-END LOAD DATA");

        /**
         * Khởi tạo tồn kho
         */
        this.baseService.dataManager.loadWarehouseInfo();

        /**
         * Khởi tạo công nợ đầu ngày cho đại lý
         */
//        this.baseService.dataManager.initDeptAgencyInfo();

        /**
         * Tính lại tồn kho
         */
//        this.baseService.dataManager.getWarehouseManager().resetTonKho();


        this.baseService.alertToTelegram("RESTARTED", ResponseStatus.SUCCESS);
    }
}