package com.iirtech.ralphlee;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IpHandler {

    public static void main(String[] args) {

        //국가를 입력하면 해당하는 아이피를 차단(CSV로 불러와서 맵) -> 2진법 변환하여 범위 검사
        //국가 리스트, 특정 아이피를 입력해서 차단할 수 있게 1차로 짜고, 2차로 그것을 확인할 수 있도록 작성 -> boolean으로 리턴
        List<String> ipList;
        String nation;
    }

    public static void handleIpList(List<String> ipList, String nation) {
        if (ipList == null || ipList.isEmpty()) {
            // IP 리스트가 비어있는 경우 처리

        } else if (!IpValidator.validateIpList(ipList)) {
            // IP 리스트에 올바르지 않은 IP 주소가 있는 경우 처리

        } else {
            // IP 리스트가 올바른 경우 처리
            try {
                IpBlocker.blockIpList(ipList); //차단 및 이어서 처리 진행
                /////
            } catch (Exception e) {
                System.err.println("IP 차단 처리 중 오류가 발생하였습니다.");
                e.printStackTrace();
            }
        }
    }
    //클라이언트 정보 취득
    public static String getClientIp(HttpServletRequest request) throws Exception {
        String clientIp = null;
        boolean isIpInHeader = false;

        List<String> headerList = new ArrayList<String>();
        headerList.add("X-Forwarded-For");
        headerList.add("HTTP_CLIENT_IP");
        headerList.add("HTTP_X_FORWARDED_FOR");
        headerList.add("HTTP_X_FORWARDED");
        headerList.add("HTTP_FORWARDED_FOR");
        headerList.add("HTTP_FORWARDED");
        headerList.add("Proxy-Client-IP");
        headerList.add("WL-Proxy-Client-IP");
        headerList.add("HTTP_VIA");
        headerList.add("IPV6_ADR");

        for (String header : headerList) {
            clientIp = request.getHeader(header);
            if (StringUtils.hasText(clientIp) && !clientIp.equals("unknown")) {
                isIpInHeader = true;
                break;
            }
        }

        if (!isIpInHeader) {
            clientIp = request.getRemoteAddr();
        }

        return clientIp;
    }

    //IP 유효성 검사
    public static class IpValidator {
        public static boolean validateIpList(List<String> ipList) {
            String ipRegex = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
            Pattern pattern = Pattern.compile(ipRegex);
            for (String ip : ipList) {
                Matcher matcher = pattern.matcher(ip);
                if (!matcher.matches()) {
                    return false;
                }
            }
            return true;
        }
    }

    //IP 차단
    public static class IpBlocker {
        public static void blockIpList(List<String> ipList) throws IOException {
            for (String ip : ipList) {
                String command = "netsh advfirewall firewall add rule name=\"Block " + ip + "\" dir=in interface=any " +
                        "action=block remoteip=" + ip;
                Process process = Runtime.getRuntime().exec(command);
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
