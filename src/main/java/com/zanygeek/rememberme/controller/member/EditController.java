package com.zanygeek.rememberme.controller.member;

import com.zanygeek.rememberme.SessionConst;
import com.zanygeek.rememberme.entity.Member;
import com.zanygeek.rememberme.entity.MemberToken;
import com.zanygeek.rememberme.form.EditEmailForm;
import com.zanygeek.rememberme.form.EditPasswordForm;
import com.zanygeek.rememberme.repository.MemberRepository;
import com.zanygeek.rememberme.repository.MemberTokenRepository;
import com.zanygeek.rememberme.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("edit")
public class EditController {
    @Autowired
    MemberService memberService;
    @Autowired
    MemberTokenRepository memberTokenRepository;
    @Autowired
    MemberRepository memberRepository;
    @GetMapping
    public String editProfile(Model model, @SessionAttribute(name = SessionConst.member, required = true) Member member, @ModelAttribute EditPasswordForm editPasswordForm,@ModelAttribute EditEmailForm editEmailForm){
        model.addAttribute("member", member);
        model.addAttribute("nowEmail", memberService.getStarsEmail(member.getEmail()));
        return "member/edit";
    }
    @PostMapping("password")
    public String editPassword(Model model, @SessionAttribute(name = SessionConst.member, required = true)Member member, @Validated EditPasswordForm editPasswordForm, BindingResult bindingResultPassword, @ModelAttribute EditEmailForm editEmailForm, RedirectAttributes attr){
        if(memberService.hasPasswordError(bindingResultPassword,editPasswordForm,member)){
            model.addAttribute("editPasswordForm", editPasswordForm);
            model.addAttribute("nowEmail", memberService.getStarsEmail(member.getEmail()));
            return"member/edit";
        }
        memberService.editPassword(member,editPasswordForm);
        attr.addFlashAttribute("passwordSuccess", true);
        return "redirect:/edit";
    }
    @PostMapping("email")
    public String editEmail(Model model, @SessionAttribute(name = SessionConst.member, required = true)Member member, @Validated EditEmailForm editEmailForm,BindingResult bindingResultEmail,@ModelAttribute EditPasswordForm editPasswordForm, RedirectAttributes attr){
        if(memberService.hasEmailError(bindingResultEmail,editEmailForm,member)){
            model.addAttribute("editEmailForm", editEmailForm);
            model.addAttribute("nowEmail", memberService.getStarsEmail(member.getEmail()));
            return"member/edit";
        }
        memberService.editEmail(member,editEmailForm);
        attr.addFlashAttribute("emailSuccess", true);
        return "redirect:/edit";
    }
    //메일 토큰 확인
    @RequestMapping("/confirmMail")
    public String confirmMail(@RequestParam("token") String token, HttpServletRequest request) {
        MemberToken checkToken = memberTokenRepository.findByConfirmToken(token);
        if (token != null) {
            Member member = memberRepository.findByUserId(checkToken.getUserId());
            member.setEmail(checkToken.getChangeEmail());
                memberRepository.save(member);
            request.getSession().setAttribute(SessionConst.member, member);
            return "redirect:/";
        } else {
            return "error";
        }
    }
}