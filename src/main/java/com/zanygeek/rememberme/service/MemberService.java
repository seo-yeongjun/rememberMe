package com.zanygeek.rememberme.service;

import com.zanygeek.rememberme.entity.Member;
import com.zanygeek.rememberme.entity.MemberToken;
import com.zanygeek.rememberme.entity.Memorial;
import com.zanygeek.rememberme.form.EditEmailForm;
import com.zanygeek.rememberme.form.EditPasswordForm;
import com.zanygeek.rememberme.form.FindForm;
import com.zanygeek.rememberme.repository.MemberRepository;
import com.zanygeek.rememberme.repository.MemberTokenRepository;
import com.zanygeek.rememberme.repository.MemorialRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Random;

@Service
@Log4j2
public class MemberService {
    @Autowired
    MemorialRepository memorialRepository;
    @Autowired
    MemorialService memorialService;
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
    @Autowired
    TemplateEngine templateEngine;

    public void editPassword(Member member, EditPasswordForm editPasswordForm) {
        member.setPassword(passwordEncoder.encode(editPasswordForm.getPassword()));
        memberRepository.save(member);
    }

    public boolean hasPasswordError(BindingResult bindingResult, EditPasswordForm editPasswordForm, Member member) {
        if (!editPasswordForm.getPassword().equals(editPasswordForm.getPassword2())) {
            bindingResult.addError(new FieldError("editPasswordForm", "password2", "???????????? ????????????."));
        }
        if (!passwordEncoder.matches(editPasswordForm.getSavedPassword(), member.getPassword())) {
            bindingResult.addError(new FieldError("editPasswordForm", "savedPassword", "????????? ????????????."));
        }
        return bindingResult.hasErrors();
    }

    public String getStarsEmail(String email) {
        String StarEmail = "";
        StarEmail += email.substring(0, 2);
        StarEmail += "*****@*****";
        StarEmail += email.substring(email.indexOf('.'));
        return StarEmail;
    }

    public boolean hasEmailError(BindingResult bindingResult, EditEmailForm editEmailForm, Member member) {
        if (!editEmailForm.getSavedEmail().equals(member.getEmail())) {
            bindingResult.addError(new FieldError("editEmailForm", "savedEmail", "???????????? ????????????."));
        }
        if (memberRepository.findByEmail(editEmailForm.getEmail()) != null) {
            bindingResult.addError(new FieldError("editEmailForm", "email", "?????? ???????????? ??????????????????."));
        }
        return bindingResult.hasErrors();
    }

    public void editEmail(Member member, EditEmailForm editEmailForm) throws MessagingException {
        //?????? ?????? ?????? ??????
        MemberToken token = new MemberToken(member.getUserId());
        token.setChangeEmail(editEmailForm.getEmail());
        token.setSavedEmail(editEmailForm.getSavedEmail());
        //??????, ?????? ??????
        try {
            memberTokenRepository.save(token);
        } catch (Exception e) {
            log.error(e);
        }
        //?????? ????????? ??????
        sendEmailConfirmMail(member, token);
    }

    //????????? ?????? ????????? ?????? ?????????
    void sendEmailConfirmMail(Member member, MemberToken token) throws MessagingException {
        MimeMessage message = mailSenderService.mimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom("zanygeek8371@xn--oy2b6m82b8p.com");
        helper.setTo(token.getChangeEmail());
        helper.setSubject("[????????????] " + member.getName() + "??????, ????????? ?????? ?????? ???????????????.");
        Context context = new Context();
        context.setVariable("name", member.getName());
        context.setVariable("url", uri);
        context.setVariable("changeURL", uri + "edit/confirmMail?token=" + token.getConfirmToken());
        String html = templateEngine.process("mail/changeMail", context);
        helper.setText(html, true);
        try {
            mailSenderService.sendMail(message);
        } catch (Exception e) {
            log.error("?????? ??????:" + e);
        }
    }

    public void sendTemporaryPassword(FindForm findForm) throws MessagingException {
        String temporaryPassword = "rememberMe";
        temporaryPassword += makeRanFiveNum()+"!";
        System.out.println(temporaryPassword);
        Member member = memberRepository.findByEmail(findForm.getEmail());
        member.setPassword(passwordEncoder.encode(temporaryPassword));
        memberRepository.save(member);
        MimeMessage message = mailSenderService.mimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom("zanygeek8371@xn--oy2b6m82b8p.com");
        helper.setTo(findForm.getEmail());
        helper.setSubject("[????????????] ?????? ???????????? ?????? ??????");
        Context context = new Context();
        context.setVariable("name", findForm.getName());
        context.setVariable("url", uri);
        context.setVariable("temporaryPassword", temporaryPassword);
        String html = templateEngine.process("mail/temporaryPassword", context);
        helper.setText(html, true);
        try {
            mailSenderService.sendMail(message);
        } catch (Exception e) {
            log.error("?????? ??????:" + e);
        }
    }

    String makeRanFiveNum(){
        Random random = new Random();
        int createNum = 0;
        String ranNum = "";
        int letter= 5;
        StringBuilder resultNum = new StringBuilder();

        for (int i=0; i<letter; i++) {
            createNum = random.nextInt(9);
            ranNum =  Integer.toString(createNum);
            resultNum.append(ranNum);
        }
        return resultNum.toString();
    }

    public void deleteMember(Member member) {
        List<Memorial> memorials = memorialRepository.findAllByMemberId(member.getId());
        for (Memorial memorial : memorials) {
            memorialService.delete(memorial);
        }
        memberRepository.delete(member);
    }

    public boolean memberIdExist(FindForm findForm) {
        return memberRepository.existsByNameAndEmail(findForm.getName(), findForm.getEmail());
    }
    public boolean memberPasswordExist(FindForm findForm) {
        return memberRepository.existsByNameAndUserIdAndEmail(findForm.getName(),findForm.getUserId(), findForm.getEmail());
    }

    public String findStarUserIdByMemberIdForm(FindForm findForm) {
        Member member = memberRepository.findByNameAndEmail(findForm.getName(), findForm.getEmail());
        String userId = member.getUserId();
        String starId = "";
        starId += userId.substring(0,2);
        starId += "****";
        starId += userId.substring(5,userId.length());
        return starId;
    }
}
