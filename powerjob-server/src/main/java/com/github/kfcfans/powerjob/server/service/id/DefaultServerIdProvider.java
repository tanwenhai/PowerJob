package com.github.kfcfans.powerjob.server.service.id;

import com.github.kfcfans.powerjob.common.utils.NetUtils;
import com.github.kfcfans.powerjob.server.extension.ServerIdProvider;
import com.github.kfcfans.powerjob.server.persistence.core.model.ServerInfoDO;
import com.github.kfcfans.powerjob.server.persistence.core.repository.ServerInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 默认服务器 ID 生成策略，不适用于 Server 频繁重启且变化 IP 的场景
 * @author user
 */
@Slf4j
@Service
public class DefaultServerIdProvider implements ServerIdProvider {
    /**
     * xxx-1,aa-bb-2
     */
    private static final Pattern HOSTNAME_PATTERN = Pattern.compile("^.*-([0-9]+)\\..+");

    private final Long id;

    public DefaultServerIdProvider(ServerInfoRepository serverInfoRepository) {
        String ip = NetUtils.getLocalHost();
        ServerInfoDO server = serverInfoRepository.findByIp(ip);

        if (server == null) {
            ServerInfoDO newServerInfo = new ServerInfoDO(ip);
            server = serverInfoRepository.saveAndFlush(newServerInfo);
        }
        this.id = server.getId();

        log.info("[DefaultServerIdProvider] address:{},id:{}", ip, id);
    }

    @Override
    public long getServerId() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            Matcher matcher = HOSTNAME_PATTERN.matcher(hostname);
            if (matcher.matches()) {
                return Long.parseLong(matcher.group(1));
            }
            throw new RuntimeException(String.format("hostname=%s not match %s", hostname, HOSTNAME_PATTERN.toString()));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
