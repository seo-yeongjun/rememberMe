package com.zanygeek.rememberme.service;

import com.zanygeek.rememberme.entity.Member;
import com.zanygeek.rememberme.entity.MemberToken;
import com.zanygeek.rememberme.form.EditEmailForm;
import com.zanygeek.rememberme.form.EditPasswordForm;
import com.zanygeek.rememberme.repository.MemberRepository;
import com.zanygeek.rememberme.repository.MemberTokenRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@Service
@Log4j2
public class MemberService {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    MemberTokenRepository memberTokenRepository;
    @Autowired
    MailSenderService mailSenderService;
    @Value("${rememberme.uri}")
    private String uri;

    public void editPassword(Member member,EditPasswordForm editPasswordForm) {
        member.setPassword(passwordEncoder.encode(editPasswordForm.getPassword()));
        memberRepository.save(member);
    }

    public boolean hasPasswordError(BindingResult bindingResult, EditPasswordForm editPasswordForm, Member member) {
        if (!editPasswordForm.getPassword().equals(editPasswordForm.getPassword2())) {
            bindingResult.addError(new FieldError("editPasswordForm", "password2", "일치하지 않습니다."));
        }
        if(!passwordEncoder.matches(editPasswordForm.getSavedPassword(),member.getPassword())){
            bindingResult.addError(new FieldError("editPasswordForm", "savedPassword", "암호가 틀립니다."));
        }
        return bindingResult.hasErrors();
    }
    public String getStarsEmail(String email){
        String StarEmail = "";
        StarEmail+=email.substring(0,2);
        StarEmail+="*****@*****";
        StarEmail+=email.substring(email.indexOf('.'));
        return StarEmail;
    }

    public boolean hasEmailError(BindingResult bindingResult, EditEmailForm editEmailForm, Member member) {
        if (!editEmailForm.getSavedEmail().equals(member.getEmail())) {
            bindingResult.addError(new FieldError("editEmailForm", "savedEmail", "일치하지 않습니다."));
        }
        if (memberRepository.findByEmail(editEmailForm.getEmail()) != null) {
            bindingResult.addError(new FieldError("editEmailForm", "email", "이미 존재하는 이메일입니다."));
        }
        return bindingResult.hasErrors();
    }

    public void editEmail(Member member, EditEmailForm editEmailForm) {
        //메일 인증 토큰 생성
        MemberToken token = new MemberToken(member.getUserId());
        token.setChangeEmail(editEmailForm.getEmail());
        token.setSavedEmail(editEmailForm.getSavedEmail());
        //멤버, 토큰 저장
        try {
            memberTokenRepository.save(token);
        } catch (Exception e) {
            log.error(e);
        }
        //인증 메시지 발송
        sendEmailConfirmMail(member, token);
    }

    //이메일 확인 메시지 발송 메서드
    void sendEmailConfirmMail(Member member, MemberToken token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("zanygeek8371@xn--oy2b6m82b8p.com");
        message.setTo(token.getChangeEmail());
        message.setSubject(member.getName() + "님의, 리멤버미 이메일 변경 안내 메일입니다.");
        message.setText("메일 변경을 위해 url을 클릭해 주세요: "
                + uri + "edit/confirmMail?token=" + token.getConfirmToken());
        try {
            mailSenderService.sendMail(message);
        } catch (Exception e) {
            log.error("에러 발생:" + e);
        }
    }
}