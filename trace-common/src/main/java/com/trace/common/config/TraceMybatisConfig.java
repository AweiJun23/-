package com.trace.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.trace.common.context.UserContext;
import java.time.LocalDateTime;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 公共配置
 * <p>
 * 统一提供：分页插件、乐观锁插件、审计字段自动填充。
 * 放在 common 模块避免各微服务重复实现，网关（无 MyBatis 依赖）通过 @ConditionalOnClass 自动跳过。
 */
@Configuration
@ConditionalOnClass(MybatisPlusInterceptor.class)
public class TraceMybatisConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 乐观锁插件：农事记录改动会触发哈希重算，必须防止并发覆盖
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    /**
     * 审计字段自动填充
     */
    @Bean
    public MetaObjectHandler traceMetaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                LocalDateTime now = LocalDateTime.now();
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
                this.strictInsertFill(metaObject, "createBy", Long.class, UserContext.getUserIdOrDefault(0L));
                this.strictInsertFill(metaObject, "updateBy", Long.class, UserContext.getUserIdOrDefault(0L));
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
                this.strictUpdateFill(metaObject, "updateBy", Long.class, UserContext.getUserIdOrDefault(0L));
            }
        };
    }

    /**
     * 雪花算法 ID 生成器（分布式部署时通过配置 workId 区分实例）
     */
    @Bean
    public DefaultIdentifierGenerator identifierGenerator() {
        return new DefaultIdentifierGenerator();
    }
}
