package com.bobooi.watch.data.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.transaction.annotation.Transactional;
import com.bobooi.watch.data.repository.DataRepository;
import com.bobooi.watch.common.component.BeanHelper;
import com.bobooi.watch.common.exception.ApplicationException;
import com.bobooi.watch.common.exception.AssertUtils;

import javax.annotation.Resource;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.bobooi.watch.common.response.SystemCodeEnum.ARGUMENT_WRONG;

/**
 * 所有通用的数据访问操作都写在这里，避免具体Service类中出现过多样板代码
 * <p>
 * TODO 把异常换成内部异常
 * @author bobo
 * @date 2021/3/31
 */
@SuppressWarnings("unchecked")
@Slf4j
public abstract class BaseDataService<Entity, Id> {
    @Resource
    protected BeanHelper beanHelper;
    @Autowired
    protected DataRepository<Entity, Id> entityRepository;

    private Class<Entity> entityClass;
    private Class<Id> idClass;

    private Type[] getRepositoryActualTypes() {
        return (((ParameterizedType) ((Class<?>) ResolvableType.forInstance(entityRepository).resolve().getGenericInterfaces()[0]).getGenericInterfaces()[0]).getActualTypeArguments());
    }

    private Class<Entity> getEntityClass() {
        if (entityClass == null) {
            entityClass = (Class<Entity>) getRepositoryActualTypes()[0];
        }
        return entityClass;
    }

    private Class<Id> getIdClass() {
        if (idClass == null) {
            idClass = (Class<Id>) getRepositoryActualTypes()[1];
        }
        return idClass;
    }

    public Entity getOneOr(Id id, Entity defaultValue) {
        return getOne(id).orElse(defaultValue);
    }

    public <X extends Throwable> Entity getOneOrThrow(Id id, Supplier<? extends X> exceptionSupplier) throws X {
        return getOne(id).orElseThrow(exceptionSupplier);
    }

    public Optional<Entity> getOne(Id id) {
        AssertUtils.notNull(id, ApplicationException.withResponse(ARGUMENT_WRONG, "ID不能为空"));
        return entityRepository.findById(id);
    }

    public List<Entity> findAll() {
        return entityRepository.findAll();
    }

    public List<Entity> findAll(Example<Entity> example) {
        return entityRepository.findAll(example);
    }

    public List<Entity> findAll(Entity exampleEntity) {
        return this.findAll(Example.of(exampleEntity));
    }

    public List<Entity> findAll(Entity exampleEntity, ExampleMatcher exampleMatcher) {
        return this.findAll(Example.of(exampleEntity, exampleMatcher));
    }


    public Optional<Entity> findOne(Example<Entity> example) {
        return entityRepository.findOne(example);
    }

    public Optional<Entity> findOne(Entity exampleEntity) {
        return this.findOne(Example.of(exampleEntity));
    }

    public Optional<Entity> findOne(Entity exampleEntity, ExampleMatcher exampleMatcher) {
        return this.findOne(Example.of(exampleEntity, exampleMatcher));
    }

    /**
     * 保存：无ID时新增，有ID时更新
     *
     * @return 保存后的对象，新增时可藉此取得ID
     */
    public Entity save(Entity entity) {
        AssertUtils.notNull(entity, ApplicationException.withResponse(ARGUMENT_WRONG, "保存对象不能为空"));
        return entityRepository.save(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Entity> save(List<Entity> entities) {
        return entities.stream().map(this::save).collect(Collectors.toList());
    }

    /**
     * 借助内省获取实体类对象的id字段值，子类可以覆写改用getId直接获取（可选）
     *
     * @param entity 实体类对象
     * @return id
     */
    public Id getEntityId(Entity entity) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(entity.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if ("id".equals(propertyDescriptor.getName())) {
                    return (Id) propertyDescriptor.getReadMethod().invoke(entity);
                }
            }
        } catch (Exception e) {
            log.error("内省获取id出错，对象：{}", entity, e);
        }
        return null;
    }

    public Entity insert(Entity entity) {
        AssertUtils.notNull(entity, ApplicationException.withResponse(ARGUMENT_WRONG, "待新增对象不能为空"));
        AssertUtils.isNull(getEntityId(entity), ApplicationException.withResponse(ARGUMENT_WRONG, "待新增对象id应为空"));
        return save(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Entity> insert(List<Entity> entities) {
        return entities.stream().map(this::insert).collect(Collectors.toList());
    }

    public Entity update(Entity entity) {
        return update(entity, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Entity> update(List<Entity> entities) {
        return entities.stream().map(this::update).collect(Collectors.toList());
    }

    /**
     * 更新对象
     *
     * @param entity        待更新对象
     * @param forceCover    是否强制覆盖，为false时只会更新entity中的非null字段，非true时直接覆盖
     * @param filedExcluded 忽略的字段（不更新）
     * @return 更新后的对象
     * @apiNote entity的id字段不能为空
     */
    public Entity update(Entity entity, boolean forceCover, String... filedExcluded) {
        AssertUtils.notNull(entity, ApplicationException.withResponse(ARGUMENT_WRONG, "待更新对象不能为空"));
        Id id = getEntityId(entity);
        AssertUtils.notNull(id, ApplicationException.withResponse(ARGUMENT_WRONG, "待更新对象id不能为空"));
        Entity toSave = getOneOrThrow(id, () -> ApplicationException.withResponse(ARGUMENT_WRONG, "数据库中不存在对应id的对象"));
        if (forceCover) {
            BeanUtils.copyProperties(entity, toSave, filedExcluded);
        } else {
            beanHelper.copyPropertiesNotNull(entity, toSave, filedExcluded);
        }
        return save(toSave);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Entity> update(List<Entity> entities, boolean forceCover, String... filedExcluded) {
        return entities.stream().map(entity -> this.update(entity, forceCover, filedExcluded)).collect(Collectors.toList());
    }

    /**
     * 删除对象
     *
     * @param entity 待删除对象
     * @apiNote 会检查对象是否已经在数据库中存在，如果不需要，直接用repository来删除就好了，没必要调这个方法
     */
    public void delete(Entity entity) {
        AssertUtils.notNull(entity, ApplicationException.withResponse(ARGUMENT_WRONG, "待删除对象不能为空"));
        Id id = getEntityId(entity);
        AssertUtils.notNull(id, ApplicationException.withResponse(ARGUMENT_WRONG, "待删除对象id不能为空"));
        AssertUtils.notNull(getOneOr(id, null), ApplicationException.withResponse(ARGUMENT_WRONG, "数据库中不存在对应id的对象"));
        entityRepository.delete(entity);
    }
}
