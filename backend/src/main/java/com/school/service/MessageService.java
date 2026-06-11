package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.Message;
import com.school.mapper.MessageMapper;
import org.springframework.stereotype.Service;

@Service
public class MessageService extends ServiceImpl<MessageMapper, Message> {
}
