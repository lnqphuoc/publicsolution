package com.app.server.database;

import com.app.server.config.ConfigInfo;
import com.app.server.data.dto.location.City;
import com.app.server.data.dto.location.District;
import com.app.server.data.dto.location.Region;
import com.app.server.data.dto.location.Ward;
import com.ygame.framework.common.LogUtil;
import com.ygame.framework.dbconn.ClientManager;
import com.ygame.framework.dbconn.ManagerIF;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class LocationDB {

    // Load all region
    public void loadAllRegion(Map<Integer, Region> mpRegion) {
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM region ORDER BY id ASC";
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        Region region = new Region();
                        region.setId(rs.getInt("id"));
                        region.setCode(rs.getString("code"));
                        region.setName(rs.getString("name"));
                        region.setStatus(rs.getInt("status"));
                        mpRegion.put(region.getId(), region);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
    }

    // Load all city
    public void loadAllCity(Map<Integer, City> mpCity, Map<Integer, Region> mpRegion) {
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM city ORDER BY name ASC, id ASC";
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        City city = new City();
                        city.setId(rs.getInt("id"));
                        city.setName(rs.getString("name"));
                        city.setCode(rs.getString("code"));
                        city.setStatus(rs.getInt("status"));
                        Integer regionId = rs.getInt("region_id");
                        if (regionId != null && mpRegion.get(regionId) != null) {
                            Region region = mpRegion.get(regionId);
                            region.getLtCity().add(city);
                            city.setRegion(region);
                        }
                        mpCity.put(city.getId(), city);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
    }

    // Load all district
    public void loadAllDistrict(Map<Integer, District> mpDistrict, Map<Integer, City> mpCity) {
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM district ORDER BY name ASC, id ASC";
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        District district = new District();
                        district.setId(rs.getInt("id"));
                        district.setName(rs.getString("name"));
                        district.setStatus(rs.getInt("status"));
                        Integer cityId = rs.getInt("city_id");
                        if (cityId != null && mpCity.get(cityId) != null) {
                            City city = mpCity.get(cityId);
                            city.getLtDistrict().add(district);
                            district.setCity(city);
                        }
                        mpDistrict.put(district.getId(), district);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
    }

    // Load all ward
    public void loadAllWard(Map<Integer, Ward> mpWard, Map<Integer, District> mpDistrict) {
        ManagerIF cm = null;
        Connection con = null;
        try {
            cm = ClientManager.getInstance(ConfigInfo.DATABASE);
            con = cm.borrowClient();
            String sql = "SELECT * FROM ward ORDER BY name ASC, id ASC";
            try (Statement stmt = con.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        Ward ward = new Ward();
                        ward.setId(rs.getInt("id"));
                        ward.setName(rs.getString("name"));
                        ward.setStatus(rs.getInt("status"));
                        Integer districtId = rs.getInt("district_id");
                        if (districtId != null && mpDistrict.get(districtId) != null) {
                            District district = mpDistrict.get(districtId);
                            district.getLtWard().add(ward);
                            ward.setDistrict(district);
                        }
                        mpWard.put(ward.getId(), ward);
                    }
                }
            }
        } catch (Exception ex) {
            LogUtil.printDebug("", ex);
        } finally {
            if (cm != null && con != null) {
                cm.returnClient(con);
            }
        }
    }
}