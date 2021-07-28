/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.summercattle.commons.utils.redis;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

@ConditionalOnClass(RedisOperations.class)
@Component
public class RedisTemplateUtils {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	public void hmset(String key, Map<String, Object> map) {
		redisTemplate.opsForHash().putAll(key, map);
	}

	public void hset(String key, Object hashKey, Object value) {
		redisTemplate.opsForHash().put(key, hashKey, value);
	}

	public boolean hexists(String key, Object hashKey) {
		return redisTemplate.opsForHash().hasKey(key, hashKey);
	}

	public Map<Object, Object> hgetall(String key) {
		return redisTemplate.opsForHash().entries(key);
	}

	public List<Object> hvals(String key) {
		return redisTemplate.opsForHash().values(key);
	}

	public Set<Object> hkeys(String key) {
		return redisTemplate.opsForHash().keys(key);
	}

	public List<Object> hmget(String key, Collection<Object> hashKeys) {
		return redisTemplate.opsForHash().multiGet(key, hashKeys);
	}

	public boolean hsetnx(String key, Object hashKey, Object value) {
		return redisTemplate.opsForHash().putIfAbsent(key, hashKey, value);
	}

	public Long hdel(String key, Object... hashKeys) {
		return redisTemplate.opsForHash().delete(key, hashKeys);
	}

	public Object hget(String key, Object hashKey) {
		return redisTemplate.opsForHash().get(key, hashKey);
	}

	public void set(String key, Object value) {
		redisTemplate.opsForValue().set(key, value);
	}

	public void set(String key, Object value, long seconds) {
		redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
	}

	public Object get(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	public Boolean del(String key) {
		return redisTemplate.delete(key);
	}

	public Long del(Collection<String> keys) {
		return redisTemplate.delete(keys);
	}

	public Long strlen(String key) {
		return redisTemplate.opsForValue().size(key);
	}

	public Object getset(String key, Object value) {
		return redisTemplate.opsForValue().getAndSet(key, value);
	}

	public String getrange(String key, long start, long end) {
		return redisTemplate.opsForValue().get(key, start, end);
	}

	public Integer append(String key, String value) {
		return redisTemplate.opsForValue().append(key, value);
	}

	public Long lpush(String key, Object... values) {
		return redisTemplate.opsForList().leftPushAll(key, values);
	}

	public Long rpush(String key, Object... values) {
		return redisTemplate.opsForList().rightPushAll(key, values);
	}

	public Object lindex(String key, long index) {
		return redisTemplate.opsForList().index(key, index);
	}

	public Long llen(String key) {
		return redisTemplate.opsForList().size(key);
	}

	public Object lpop(String key) {
		return redisTemplate.opsForList().leftPop(key);
	}

	public Object rpop(String key) {
		return redisTemplate.opsForList().rightPop(key);
	}

	public Long lpushx(String key, Object value) {
		return redisTemplate.opsForList().leftPushIfPresent(key, value);
	}

	public Long rpushx(String key, Object value) {
		return redisTemplate.opsForList().rightPushIfPresent(key, value);
	}

	public List<Object> lrange(String key, long start, long end) {
		return redisTemplate.opsForList().range(key, start, end);
	}

	public Long lrem(String key, long count, Object value) {
		return redisTemplate.opsForList().remove(key, count, value);
	}

	public void lset(String key, long index, Object value) {
		redisTemplate.opsForList().set(key, index, value);
	}

	public Long sadd(String key, Object... values) {
		return redisTemplate.opsForSet().add(key, values);
	}

	public Long scard(String key) {
		return redisTemplate.opsForSet().size(key);
	}

	public Set<Object> sidff(Collection<String> keys) {
		return redisTemplate.opsForSet().difference(keys);
	}

	public Set<Object> sinter(Collection<String> keys) {
		return redisTemplate.opsForSet().intersect(keys);
	}

	public Set<Object> sunion(Collection<String> keys) {
		return redisTemplate.opsForSet().union(keys);
	}

	public Long sdiffstore(String key, String otherKey, String destKey) {
		return redisTemplate.opsForSet().differenceAndStore(key, otherKey, destKey);
	}

	public Long sinterstore(String key, String otherKey, String destKey) {
		return redisTemplate.opsForSet().intersectAndStore(key, otherKey, destKey);
	}

	public Long sunionstore(String key, String otherKey, String destKey) {
		return redisTemplate.opsForSet().unionAndStore(key, otherKey, destKey);
	}

	public Boolean sismember(String key, Object value) {
		return redisTemplate.opsForSet().isMember(key, value);
	}

	public Set<Object> smembers(String key) {
		return redisTemplate.opsForSet().members(key);
	}

	public Object spop(String key) {
		return redisTemplate.opsForSet().pop(key);
	}

	public Object srandmember(String key) {
		return redisTemplate.opsForSet().randomMember(key);
	}

	public List<Object> srandmember(String key, long count) {
		return redisTemplate.opsForSet().randomMembers(key, count);
	}

	public Long srem(String key, Object... values) {
		return redisTemplate.opsForSet().remove(key, values);
	}

	public boolean exists(String key) {
		return redisTemplate.hasKey(key);
	}

	public Set<String> keys(String pattern) {
		return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
			Set<String> keys = new HashSet<String>();
			try (Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(pattern).count(Long.MAX_VALUE).build())) {
				while (cursor.hasNext()) {
					keys.add(StringUtils.newStringUtf8(cursor.next()));
				}
			}
			return keys;
		});
	}
}